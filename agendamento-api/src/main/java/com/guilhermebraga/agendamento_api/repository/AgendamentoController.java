package com.guilhermebraga.agendamento_api.controller;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
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
 * Controller REST da entidade Agendamento.
 * Base URL: /api/v1/agendamentos
 *
 * @author Guilherme Braga
 */
@RestController
@RequestMapping("/api/v1/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    /**
     * POST /api/v1/agendamentos — Cria novo agendamento.
     */
    @Operation(summary = "Criar novo agendamento",
            description = "Cria um agendamento validando disponibilidade do profissional no horário solicitado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente, Profissional ou Serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de horário para o profissional", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(
            @Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendamentoService.criar(request));
    }

    /**
     * GET /api/v1/agendamentos — Lista todos os agendamentos.
     */
    @Operation(summary = "Listar todos os agendamentos",
            description = "Retorna a lista completa de agendamentos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = AgendamentoResponse.class)))
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listarTodos() {
        return ResponseEntity.ok(agendamentoService.listarTodos());
    }

    /**
     * GET /api/v1/agendamentos/{id} — Busca agendamento por ID.
     */
    @Operation(summary = "Buscar agendamento por ID",
            description = "Retorna os dados completos de um agendamento pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscarPorId(
            @Parameter(description = "ID do agendamento", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    /**
     * PUT /api/v1/agendamentos/{id} — Atualiza agendamento existente.
     */
    @Operation(summary = "Atualizar agendamento",
            description = "Atualiza os dados de um agendamento. Não permitido para agendamentos CONCLUIDOS ou CANCELADOS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de horário ou status inválido", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> atualizar(
            @Parameter(description = "ID do agendamento", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(agendamentoService.atualizar(id, request));
    }

    /**
     * PATCH /api/v1/agendamentos/{id}/status — Atualiza apenas o status.
     */
    @Operation(summary = "Atualizar status do agendamento",
            description = "Atualiza o status seguindo o fluxo: AGENDADO → CONFIRMADO → CONCLUIDO ou CANCELADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Transição de status inválida", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AgendamentoResponse> atualizarStatus(
            @Parameter(description = "ID do agendamento", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novo status do agendamento", required = true)
            @RequestParam StatusAgendamento status) {
        return ResponseEntity.ok(agendamentoService.atualizarStatus(id, status));
    }

    /**
     * DELETE /api/v1/agendamentos/{id} — Cancela agendamento.
     */
    @Operation(summary = "Cancelar agendamento",
            description = "Cancela um agendamento ativo. Não permitido para agendamentos já CONCLUIDOS.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agendamento cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Agendamento não pode ser cancelado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "ID do agendamento", required = true)
            @PathVariable Long id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}