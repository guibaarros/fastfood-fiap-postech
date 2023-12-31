package com.guibaarros.fiap.postech.fastfood.adapters.controller;

import com.guibaarros.fiap.postech.fastfood.adapters.dtos.errorhandler.ErrorDTO;
import com.guibaarros.fiap.postech.fastfood.adapters.dtos.order.OrderRequestDTO;
import com.guibaarros.fiap.postech.fastfood.adapters.dtos.order.OrderResponseDTO;
import com.guibaarros.fiap.postech.fastfood.application.port.incoming.order.ConfirmPaymentUseCase;
import com.guibaarros.fiap.postech.fastfood.application.port.incoming.order.CreateOrderUseCase;
import com.guibaarros.fiap.postech.fastfood.application.port.incoming.order.ListQueuedOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
@Tag(name = "Order Controller")
@Validated
@Slf4j
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ListQueuedOrderUseCase listQueuedOrderUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @Operation(summary = "Criar um novo pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Pedido criado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class))})
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody final OrderRequestDTO orderRequestDTO) {

        log.info("create new order; orderRequestDTO={}", orderRequestDTO);
        final OrderResponseDTO orderResponseDTO = createOrderUseCase(orderRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDTO);
    }

    private OrderResponseDTO createOrderUseCase(final OrderRequestDTO orderRequestDTO) {
        if (Objects.isNull(orderRequestDTO.getClientId())) {
            log.info("create new order without client");
            return createOrderUseCase.createOrder(orderRequestDTO.getProductIds());
        }

        log.info("create new order with client; orderRequestDTO.getClientId()={}", orderRequestDTO.getClientId());
        return createOrderUseCase.createOrder(orderRequestDTO.getClientId(), orderRequestDTO.getProductIds());
    }

    @Operation(summary = "Busca pedidos em preparação")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Pedido(s) encontrado(s)",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDTO.class))}),
            @ApiResponse(responseCode = "404",
                    description = "Nenhum pedido em preparo encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class))})
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponseDTO>> findOrderInPreparation() {
        log.info("finding orders in preparation");
        final List<OrderResponseDTO> productResponseDTOList =
                listQueuedOrderUseCase.listQueuedOrderUseCase();
        return ResponseEntity.status(HttpStatus.OK).body(productResponseDTOList);
    }

    @Operation(summary = "Confirma pagamento do pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Pagamento do pedido confirmado"),
            @ApiResponse(responseCode = "404",
                    description = "Nenhum pedido aguardando pagamento encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class))})
    })
    @PostMapping(value = "{id}/payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> confirmOrderPayment(@PathVariable("id") final Long id) {
        log.info("confirmOrderPayment; orderId={}", id);
        confirmPaymentUseCase.confirmPayment(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
