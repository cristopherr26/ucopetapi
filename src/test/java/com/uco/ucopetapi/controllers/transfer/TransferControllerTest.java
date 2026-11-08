package com.uco.ucopetapi.controllers.transfer;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import com.uco.ucopetapi.service.transfer.TransferService;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferRequestException;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferStateException;
import com.uco.ucopetapi.service.transfer.exception.TransferNotFoundException;

/**
 * Prueba el controlador y su manejador de excepciones con MockMvc en modo "standalone":
 * no levanta Spring, ni la base, ni RabbitMQ, ni la seguridad. El usuario autenticado se
 * simula pasando un Authentication directamente a la peticion.
 */
@SuppressWarnings("java:S2187") // los tests estan en clases @Nested
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferController + TransferExceptionHandler (codigos HTTP y mensajes)")
class TransferControllerTest {

    private static final String URL = "/api/transfers";

    @Mock
    private TransferService transferService;

    private MockMvc mvc;

    private final UUID userId = UUID.randomUUID();
    private final UUID transferId = UUID.randomUUID();
    private final UUID originId = UUID.randomUUID();
    private final UUID destinationId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Authentication auth = new UsernamePasswordAuthenticationToken(userId.toString(), null);

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TransferController(transferService))
                .setControllerAdvice(new TransferExceptionHandler())
                .build();
    }

    // ------------------------------------------------------------ POST

    @Nested
    @DisplayName("POST /api/transfers")
    class Create {

        @Test
        @DisplayName("con un cuerpo valido responde 201 y usa el usuario autenticado")
        void created() throws Exception {
            when(transferService.createTransfer(any(TransferRequestDTO.class), eq(userId)))
                    .thenReturn(response(TransferStatus.PENDING));

            mvc.perform(post(URL).principal(auth).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(transferId.toString()))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.quantity").value(4));

            verify(transferService).createTransfer(any(TransferRequestDTO.class), eq(userId));
        }

        @Test
        @DisplayName("sin cantidad responde 400 y nombra el campo, sin llamar al servicio")
        void missingQuantity() throws Exception {
            String body = """
                    {"originHeadquarterId":"%s","destinationHeadquarterId":"%s","productId":"%s"}
                    """.formatted(originId, destinationId, productId);

            mvc.perform(post(URL).principal(auth).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("cantidad")));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("con cantidad cero responde 400")
        void zeroQuantity() throws Exception {
            mvc.perform(post(URL).principal(auth).contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWith("0", "\"ok\"")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("mayor a 0")));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("con observaciones de mas de 500 caracteres responde 400")
        void observationsTooLong() throws Exception {
            mvc.perform(post(URL).principal(auth).contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWith("4", "\"" + "a".repeat(501) + "\"")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("500")));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("con un JSON roto responde 400 con mensaje claro")
        void malformedJson() throws Exception {
            mvc.perform(post(URL).principal(auth).contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("JSON valido")));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("si el servicio rechaza la peticion (stock, sede, etc.) responde 400 con su mensaje")
        void businessRejection() throws Exception {
            when(transferService.createTransfer(any(TransferRequestDTO.class), eq(userId)))
                    .thenThrow(new InvalidTransferRequestException("No hay suficiente stock"));

            mvc.perform(post(URL).principal(auth).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No hay suficiente stock"));
        }
    }

    // ------------------------------------------------------------ GET

    @Nested
    @DisplayName("GET /api/transfers")
    class Read {

        @Test
        @DisplayName("por id existente responde 200")
        void findById() throws Exception {
            when(transferService.findById(transferId)).thenReturn(response(TransferStatus.IN_PROGRESS));

            mvc.perform(get(URL + "/{id}", transferId).principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.originHeadquarter.name").value("Sede origen"));
        }

        @Test
        @DisplayName("por id que no existe responde 404 con el mensaje del servicio")
        void findByIdNotFound() throws Exception {
            when(transferService.findById(transferId))
                    .thenThrow(new TransferNotFoundException("No existe un traslado con id " + transferId));

            mvc.perform(get(URL + "/{id}", transferId).principal(auth))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value(containsString(transferId.toString())));
        }

        @Test
        @DisplayName("con un id que no es UUID responde 400 y nombra el parametro")
        void invalidUuid() throws Exception {
            mvc.perform(get(URL + "/no-es-un-uuid").principal(auth))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El parametro 'id' tiene un valor invalido"));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("el listado pasa los filtros al servicio")
        void filters() throws Exception {
            when(transferService.findByFilter(TransferStatus.PENDING, originId, null))
                    .thenReturn(List.of(response(TransferStatus.PENDING)));

            mvc.perform(get(URL).principal(auth)
                            .param("status", "PENDING")
                            .param("originHeadquarterId", originId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(transferId.toString()));
        }

        @Test
        @DisplayName("el listado sin filtros responde 200")
        void noFilters() throws Exception {
            when(transferService.findByFilter(null, null, null)).thenReturn(List.of());

            mvc.perform(get(URL).principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("con un estado que no existe responde 400 y nombra el parametro")
        void invalidStatusFilter() throws Exception {
            mvc.perform(get(URL).principal(auth).param("status", "INVENTADO"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El parametro 'status' tiene un valor invalido"));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("un ResponseStatusException conserva su codigo y su razon")
        void responseStatusException() throws Exception {
            when(transferService.findById(transferId))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso"));

            mvc.perform(get(URL + "/{id}", transferId).principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("No tienes permiso"));
        }
    }

    // ------------------------------------------------------------ PUT

    @Nested
    @DisplayName("PUT /api/transfers/{id}")
    class Update {

        @Test
        @DisplayName("con un cuerpo valido responde 200")
        void updated() throws Exception {
            when(transferService.updateTransfer(eq(transferId), any(TransferRequestDTO.class), eq(userId)))
                    .thenReturn(response(TransferStatus.PENDING));

            mvc.perform(put(URL + "/{id}", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(transferId.toString()));
        }

        @Test
        @DisplayName("si el traslado ya no esta PENDING responde 409")
        void conflict() throws Exception {
            when(transferService.updateTransfer(eq(transferId), any(TransferRequestDTO.class), eq(userId)))
                    .thenThrow(new InvalidTransferStateException("Solo se puede editar un traslado en estado PENDING"));

            mvc.perform(put(URL + "/{id}", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value(containsString("PENDING")));
        }

        @Test
        @DisplayName("si no existe responde 404")
        void notFound() throws Exception {
            when(transferService.updateTransfer(eq(transferId), any(TransferRequestDTO.class), eq(userId)))
                    .thenThrow(new TransferNotFoundException("No existe"));

            mvc.perform(put(URL + "/{id}", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------ PATCH status

    @Nested
    @DisplayName("PATCH /api/transfers/{id}/status")
    class ChangeStatus {

        @Test
        @DisplayName("con un estado valido responde 200")
        void changed() throws Exception {
            when(transferService.updateTransferStatus(transferId, TransferStatus.IN_PROGRESS, userId))
                    .thenReturn(response(TransferStatus.IN_PROGRESS));

            mvc.perform(patch(URL + "/{id}/status", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("sin estado responde 400")
        void missingStatus() throws Exception {
            mvc.perform(patch(URL + "/{id}/status", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("estado")));

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("con un estado que no existe responde 400")
        void unknownStatus() throws Exception {
            mvc.perform(patch(URL + "/{id}/status", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INVENTADO\"}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(transferService);
        }

        @Test
        @DisplayName("una transicion invalida responde 409")
        void invalidTransition() throws Exception {
            when(transferService.updateTransferStatus(transferId, TransferStatus.COMPLETED, userId))
                    .thenThrow(new InvalidTransferStateException("Transicion de estado invalida"));

            mvc.perform(patch(URL + "/{id}/status", transferId).principal(auth)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Transicion de estado invalida"));
        }
    }

    // ------------------------------------------------------------ PATCH cancel

    @Nested
    @DisplayName("PATCH /api/transfers/{id}/cancel")
    class Cancel {

        @Test
        @DisplayName("responde 204 sin cuerpo y usa el usuario autenticado")
        void cancelled() throws Exception {
            mvc.perform(patch(URL + "/{id}/cancel", transferId).principal(auth))
                    .andExpect(status().isNoContent());

            verify(transferService).cancelTransfer(transferId, userId);
        }

        @Test
        @DisplayName("si el traslado ya esta cerrado responde 409")
        void conflict() throws Exception {
            doThrow(new InvalidTransferStateException("Solo se puede cancelar un traslado Pendiente o En Proceso"))
                    .when(transferService).cancelTransfer(transferId, userId);

            mvc.perform(patch(URL + "/{id}/cancel", transferId).principal(auth))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("cancelar")));
        }

        @Test
        @DisplayName("si no existe responde 404")
        void notFound() throws Exception {
            doThrow(new TransferNotFoundException("No existe"))
                    .when(transferService).cancelTransfer(transferId, userId);

            mvc.perform(patch(URL + "/{id}/cancel", transferId).principal(auth))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------ ayudantes

    private String validBody() {
        return bodyWith("4", "\"observacion\"");
    }

    private String bodyWith(String quantity, String observations) {
        return """
                {"originHeadquarterId":"%s","destinationHeadquarterId":"%s","productId":"%s",
                 "quantity":%s,"observations":%s}
                """.formatted(originId, destinationId, productId, quantity, observations);
    }

    private TransferResponseDTO response(TransferStatus status) {
        return new TransferResponseDTO(
                transferId,
                new RelatedEntityDTO(originId, "Sede origen"),
                new RelatedEntityDTO(destinationId, "Sede destino"),
                new RelatedEntityDTO(productId, null),
                4,
                status,
                "observacion",
                new RelatedEntityDTO(userId, "Ana Prueba"),
                LocalDateTime.of(2026, 11, 7, 12, 0),
                null,
                null);
    }
}