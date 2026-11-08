package com.uco.ucopetapi.service.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.uco.ucopetapi.domain.transfer.TransferDomain;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import com.uco.ucopetapi.dto.person.DocumentType;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.dto.transfers.TransferCancelMessageDTO;
import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
import com.uco.ucopetapi.dto.transfers.TransferSaveMessageDTO;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import com.uco.ucopetapi.dto.transfers.TransferStatusMessageDTO;
import com.uco.ucopetapi.messaging.transfer.TransferCancelSenderBroker;
import com.uco.ucopetapi.messaging.transfer.TransferSaveSenderBroker;
import com.uco.ucopetapi.messaging.transfer.TransferStatusSenderBroker;
import com.uco.ucopetapi.repository.transfer.ITransferRepository;
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.product.StockService;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferRequestException;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferStateException;
import com.uco.ucopetapi.service.transfer.exception.TransferNotFoundException;

@SuppressWarnings("java:S2187") // los tests estan en clases @Nested
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService (unitario, sin base de datos ni RabbitMQ)")
class TransferServiceTest {

    @Mock
    private ITransferRepository transferRepository;

    @Mock
    private HeadquarterService headquarterService;

    @Mock
    private PersonService personService;

    @Mock
    private StockService stockService;

    @Mock
    private TransferSaveSenderBroker saveSender;

    @Mock
    private TransferStatusSenderBroker statusSender;

    @Mock
    private TransferCancelSenderBroker cancelSender;

    private TransferService service;

    private final UUID originId = UUID.randomUUID();
    private final UUID destinationId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID transferId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TransferService(transferRepository, headquarterService, personService, stockService,
                saveSender, statusSender, cancelSender);

        // Camino feliz por defecto: cada test cambia solo lo que quiere probar.
        lenient().when(headquarterService.findById(originId))
                .thenReturn(headquarter(originId, "Sede origen", true));
        lenient().when(headquarterService.findById(destinationId))
                .thenReturn(headquarter(destinationId, "Sede destino", true));
        lenient().when(personService.findById(userId)).thenReturn(person(userId));
        lenient().when(stockService.findByProductAndHeadquarter(productId, originId))
                .thenReturn(stock(10));
    }

    // ------------------------------------------------------------ crear

    @Nested
    @DisplayName("Crear (publica en la cola, no guarda)")
    class Create {

        @Test
        @DisplayName("con datos validos publica un mensaje PENDING y responde con nombres, sin tocar la base")
        void publishesPendingTransfer() {
            TransferResponseDTO response = service.createTransfer(request(originId, destinationId, 4), userId);

            ArgumentCaptor<TransferSaveMessageDTO> captor = ArgumentCaptor.forClass(TransferSaveMessageDTO.class);
            verify(saveSender).execute(captor.capture(), anyString());
            TransferSaveMessageDTO message = captor.getValue();
            assertThat(message.id()).isNotNull();
            assertThat(message.originHeadquarterId()).isEqualTo(originId);
            assertThat(message.destinationHeadquarterId()).isEqualTo(destinationId);
            assertThat(message.productId()).isEqualTo(productId);
            assertThat(message.quantity()).isEqualTo(4);
            assertThat(message.status()).isEqualTo(TransferStatus.PENDING);
            assertThat(message.performedBy()).isEqualTo(userId);

            assertThat(response.id()).isEqualTo(message.id());
            assertThat(response.status()).isEqualTo(TransferStatus.PENDING);
            assertThat(response.originHeadquarter().name()).isEqualTo("Sede origen");
            assertThat(response.destinationHeadquarter().name()).isEqualTo("Sede destino");
            assertThat(response.createdBy().name()).isEqualTo("Ana Prueba");
            assertThat(response.updatedBy()).isNull();
            verifyNoInteractions(transferRepository, statusSender, cancelSender);
        }

        @Test
        @DisplayName("la sede de origen y la de destino no pueden ser la misma")
        void sameHeadquarterIsRejected() {
            TransferRequestDTO transferRequest = request(originId, originId, 4);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("no pueden ser la misma");

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("una sede inactiva no se puede usar")
        void inactiveHeadquarterIsRejected() {
            when(headquarterService.findById(destinationId))
                    .thenReturn(headquarter(destinationId, "Sede cerrada", false));

            TransferRequestDTO transferRequest = request(originId, destinationId, 4);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("Sede cerrada")
                    .hasMessageContaining("no esta activa");

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("una sede que no existe se rechaza como peticion invalida")
        void unknownHeadquarterIsRejected() {
            when(headquarterService.findById(originId)).thenThrow(new TransferNotFoundException("no existe"));

            TransferRequestDTO transferRequest = request(originId, destinationId, 4);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("No existe una sede");

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("un usuario que no existe se rechaza")
        void unknownUserIsRejected() {
            when(personService.findById(userId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

            TransferRequestDTO transferRequest = request(originId, destinationId, 4);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("No existe un usuario");

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("sin stock suficiente en el origen se rechaza y dice cuanto hay")
        void insufficientStockIsRejected() {
            when(stockService.findByProductAndHeadquarter(productId, originId)).thenReturn(stock(2));

            TransferRequestDTO transferRequest = request(originId, destinationId, 5);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("Disponible: 2")
                    .hasMessageContaining("solicitado: 5");

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("un stock con cantidad null cuenta como cero")
        void nullStockCountsAsZero() {
            when(stockService.findByProductAndHeadquarter(productId, originId)).thenReturn(stock(null));

            TransferRequestDTO transferRequest = request(originId, destinationId, 1);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("Disponible: 0");

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("un producto sin stock registrado en el origen se rechaza")
        void productWithoutStockIsRejected() {
            when(stockService.findByProductAndHeadquarter(productId, originId))
                    .thenThrow(new NoSuchElementException("El producto no tiene stock en esa sede"));

            TransferRequestDTO transferRequest = request(originId, destinationId, 1);
            assertThatThrownBy(() -> service.createTransfer(transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("no tiene stock");

            verifyNoInteractions(saveSender);
        }
    }

    // ------------------------------------------------------------ editar

    @Nested
    @DisplayName("Editar (solo mientras esta PENDING)")
    class Update {

        @Test
        @DisplayName("publica los datos nuevos con el mismo id y no modifica la entidad cargada")
        void publishesChanges() {
            TransferDomain existing = transfer(TransferStatus.PENDING);
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(existing));

            TransferResponseDTO response = service.updateTransfer(transferId,
                    request(originId, destinationId, 3), userId);

            ArgumentCaptor<TransferSaveMessageDTO> captor = ArgumentCaptor.forClass(TransferSaveMessageDTO.class);
            verify(saveSender).execute(captor.capture(), anyString());
            assertThat(captor.getValue().id()).isEqualTo(transferId);
            assertThat(captor.getValue().quantity()).isEqualTo(3);
            assertThat(captor.getValue().performedBy()).isEqualTo(userId);

            assertThat(response.quantity()).isEqualTo(3);
            assertThat(response.updatedBy().id()).isEqualTo(userId);
            assertThat(existing.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("un traslado que no existe da TransferNotFoundException")
        void unknownTransfer() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

            TransferRequestDTO transferRequest = request(originId, destinationId, 3);
            assertThatThrownBy(() -> service.updateTransfer(transferId, transferRequest, userId))
                    .isInstanceOf(TransferNotFoundException.class);

            verifyNoInteractions(saveSender);
        }

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("si ya no esta PENDING no se puede editar y no se publica nada")
        void onlyPendingCanBeEdited(TransferStatus status) {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(status)));

            TransferRequestDTO transferRequest = request(originId, destinationId, 3);
            assertThatThrownBy(() -> service.updateTransfer(transferId, transferRequest, userId))
                    .isInstanceOf(InvalidTransferStateException.class)
                    .hasMessageContaining("Estado actual: " + status);

            verifyNoInteractions(saveSender);
        }

        @Test
        @DisplayName("al editar tambien se valida el stock con la cantidad nueva")
        void stockIsValidatedAgain() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(TransferStatus.PENDING)));

            TransferRequestDTO transferRequest = request(originId, destinationId, 99);
            assertThatThrownBy(() -> service.updateTransfer(transferId, transferRequest, userId))
                    .isInstanceOf(InvalidTransferRequestException.class)
                    .hasMessageContaining("Disponible: 10");

            verifyNoInteractions(saveSender);
        }
    }

    // ------------------------------------------------------------ cambiar estado

    @Nested
    @DisplayName("Cambiar de estado")
    class ChangeStatus {

        @ParameterizedTest
        @CsvSource({"PENDING,IN_PROGRESS", "IN_PROGRESS,COMPLETED"})
        @DisplayName("las transiciones permitidas publican el mensaje y responden con el estado nuevo")
        void validTransitions(TransferStatus current, TransferStatus next) {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(current)));

            TransferResponseDTO response = service.updateTransferStatus(transferId, next, userId);

            ArgumentCaptor<TransferStatusMessageDTO> captor = ArgumentCaptor.forClass(TransferStatusMessageDTO.class);
            verify(statusSender).execute(captor.capture(), anyString());
            assertThat(captor.getValue().id()).isEqualTo(transferId);
            assertThat(captor.getValue().status()).isEqualTo(next);
            assertThat(captor.getValue().performedBy()).isEqualTo(userId);
            assertThat(response.status()).isEqualTo(next);
            assertThat(response.updatedBy().id()).isEqualTo(userId);
        }

        @ParameterizedTest
        @CsvSource({
                "PENDING,COMPLETED",
                "PENDING,PENDING",
                "PENDING,CANCELLED",
                "IN_PROGRESS,PENDING",
                "IN_PROGRESS,IN_PROGRESS",
                "IN_PROGRESS,CANCELLED"})
        @DisplayName("cualquier otra transicion da InvalidTransferStateException y no publica")
        void invalidTransitions(TransferStatus current, TransferStatus next) {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(current)));

            assertThatThrownBy(() -> service.updateTransferStatus(transferId, next, userId))
                    .isInstanceOf(InvalidTransferStateException.class)
                    .hasMessageContaining("Transicion de estado invalida");

            verifyNoInteractions(statusSender);
        }

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = {"COMPLETED", "CANCELLED"})
        @DisplayName("un traslado cerrado (COMPLETED o CANCELLED) ya no cambia de estado")
        void closedTransfersAreFinal(TransferStatus closed) {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(closed)));

            assertThatThrownBy(() -> service.updateTransferStatus(transferId, TransferStatus.IN_PROGRESS, userId))
                    .isInstanceOf(InvalidTransferStateException.class)
                    .hasMessageContaining("Pendiente o En Proceso");

            verifyNoInteractions(statusSender);
        }

        @Test
        @DisplayName("un traslado que no existe da TransferNotFoundException")
        void unknownTransfer() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateTransferStatus(transferId, TransferStatus.IN_PROGRESS, userId))
                    .isInstanceOf(TransferNotFoundException.class);

            verifyNoInteractions(statusSender);
        }

        @Test
        @DisplayName("un usuario que no existe se rechaza antes de publicar")
        void unknownUser() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(TransferStatus.PENDING)));
            when(personService.findById(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

            assertThatThrownBy(() -> service.updateTransferStatus(transferId, TransferStatus.IN_PROGRESS, userId))
                    .isInstanceOf(InvalidTransferRequestException.class);

            verifyNoInteractions(statusSender);
        }
    }

    // ------------------------------------------------------------ cancelar

    @Nested
    @DisplayName("Cancelar")
    class Cancel {

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = {"PENDING", "IN_PROGRESS"})
        @DisplayName("un traslado abierto (PENDING o IN_PROGRESS) publica la cancelacion")
        void openTransfersCanBeCancelled(TransferStatus status) {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(status)));

            service.cancelTransfer(transferId, userId);

            ArgumentCaptor<TransferCancelMessageDTO> captor = ArgumentCaptor.forClass(TransferCancelMessageDTO.class);
            verify(cancelSender).execute(captor.capture(), anyString());
            assertThat(captor.getValue().id()).isEqualTo(transferId);
            assertThat(captor.getValue().performedBy()).isEqualTo(userId);
        }

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = {"COMPLETED", "CANCELLED"})
        @DisplayName("uno cerrado no se puede cancelar y no se publica nada")
        void closedTransfersCannotBeCancelled(TransferStatus status) {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(status)));

            assertThatThrownBy(() -> service.cancelTransfer(transferId, userId))
                    .isInstanceOf(InvalidTransferStateException.class)
                    .hasMessageContaining("cancelar");

            verifyNoInteractions(cancelSender);
        }

        @Test
        @DisplayName("un traslado que no existe da TransferNotFoundException")
        void unknownTransfer() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelTransfer(transferId, userId))
                    .isInstanceOf(TransferNotFoundException.class);

            verifyNoInteractions(cancelSender);
        }
    }

    // ------------------------------------------------------------ lecturas

    @Nested
    @DisplayName("Lecturas")
    class Reads {

        @Test
        @DisplayName("findById arma la respuesta con los nombres de sedes y personas")
        void findByIdBuildsNames() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(TransferStatus.PENDING)));

            TransferResponseDTO response = service.findById(transferId);

            assertThat(response.id()).isEqualTo(transferId);
            assertThat(response.originHeadquarter().name()).isEqualTo("Sede origen");
            assertThat(response.destinationHeadquarter().name()).isEqualTo("Sede destino");
            assertThat(response.createdBy().name()).isEqualTo("Ana Prueba");
        }

        @Test
        @DisplayName("findById de un id que no existe da TransferNotFoundException")
        void findByIdUnknown() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(transferId))
                    .isInstanceOf(TransferNotFoundException.class)
                    .hasMessageContaining(transferId.toString());
        }

        @Test
        @DisplayName("si no se encuentra el nombre de una sede, responde con nombre null en vez de fallar")
        void missingNameDoesNotBreakTheResponse() {
            when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer(TransferStatus.PENDING)));
            when(headquarterService.findById(originId)).thenThrow(new TransferNotFoundException("no existe"));

            TransferResponseDTO response = service.findById(transferId);

            assertThat(response.originHeadquarter().id()).isEqualTo(originId);
            assertThat(response.originHeadquarter().name()).isNull();
        }

        @Test
        @DisplayName("findByFilter pasa los filtros al repositorio y consulta cada sede y persona una sola vez")
        void findByFilterCachesNames() {
            TransferDomain first = transfer(TransferStatus.PENDING);
            TransferDomain second = transfer(TransferStatus.PENDING);
            second.setId(UUID.randomUUID());
            when(transferRepository.findByFilter(TransferStatus.PENDING, originId, null))
                    .thenReturn(List.of(first, second));

            List<TransferResponseDTO> result = service.findByFilter(TransferStatus.PENDING, originId, null);

            assertThat(result).hasSize(2);
            verify(headquarterService, times(1)).findById(originId);
            verify(headquarterService, times(1)).findById(destinationId);
            verify(personService, times(1)).findById(userId);
        }
    }

    // ------------------------------------------------------------ ayudantes

    private TransferRequestDTO request(UUID origin, UUID destination, int quantity) {
        return new TransferRequestDTO(origin, destination, productId, quantity, "observacion");
    }

    private TransferDomain transfer(TransferStatus status) {
        TransferDomain transfer = new TransferDomain();
        transfer.setId(transferId);
        transfer.setOriginHeadquarterId(originId);
        transfer.setDestinationHeadquarterId(destinationId);
        transfer.setProductId(productId);
        transfer.setQuantity(5);
        transfer.setStatus(status);
        transfer.setObservations("observacion");
        transfer.setCreatedBy(userId);
        transfer.setCreatedAt(LocalDateTime.now());
        return transfer;
    }

    private static HeadquarterDTO headquarter(UUID id, String name, boolean active) {
        return new HeadquarterDTO(id, name, "Calle 1 # 2-3", active);
    }

    private static PersonDTO person(UUID id) {
        return new PersonDTO(id, DocumentType.CC, "10203040", "Ana", "Prueba", "ana@ucopet.com",
                null, null, false, true, null);
    }

    private StockDTO stock(Integer quantity) {
        return new StockDTO(UUID.randomUUID(), productId, originId, quantity);
    }
}