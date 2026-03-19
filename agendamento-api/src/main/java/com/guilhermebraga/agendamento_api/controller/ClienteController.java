package com.guilhermebraga.agendamento_api.controller;

import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.dto.response.ClienteResponse;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST da entidade Cliente.
 * Base URL: /api/v1/clientes
 *
 * @author Guilherme Braga
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * POST /api/v1/clientes — Cadastra novo cliente.
     */
    @Operation(summary = "Cadastrar novo cliente",
            description = "Cria um novo cliente validando CPF e e-mail únicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(request));
    }

    /**
     * GET /api/v1/clientes — Lista todos os clientes.
     */
    @Operation(summary = "Listar todos os clientes",
            description = "Retorna a lista completa de clientes cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = ClienteResponse.class)))
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    /**
     * GET /api/v1/clientes/{id} — Busca cliente por ID.
     */
    @Operation(summary = "Buscar cliente por ID",
            description = "Retorna os dados de um cliente pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @Parameter(description = "ID do cliente", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    /**
     * PUT /api/v1/clientes/{id} — Atualiza cliente existente.
     */
    @Operation(summary = "Atualizar cliente",
            description = "Atualiza os dados de um cliente existente pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já pertence a outro cliente", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @Parameter(description = "ID do cliente", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    /**
     * DELETE /api/v1/clientes/{id} — Remove cliente.
     */
    @Operation(summary = "Remover cliente",
            description = "Remove permanentemente um cliente pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do cliente", required = true)
            @PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}