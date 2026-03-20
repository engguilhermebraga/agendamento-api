package com.guilhermebraga.agendamento_api.controller;

import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.service.ServicoService;
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
 * Controller REST da entidade Serviço.
 * Base URL: /api/v1/servicos
 *
 * @author Guilherme Braga
 */
@RestController
@RequestMapping("/api/v1/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços")
public class ServicoController {

    private final ServicoService servicoService;

    /**
     * POST /api/v1/servicos — Cadastra novo serviço.
     */
    @Operation(summary = "Cadastrar novo serviço",
            description = "Cria um novo serviço validando unicidade do nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Nome já cadastrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ServicoResponse> criar(
            @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicoService.criar(request));
    }

    /**
     * GET /api/v1/servicos — Lista todos os serviços.
     */
    @Operation(summary = "Listar todos os serviços",
            description = "Retorna a lista completa de serviços cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = ServicoResponse.class)))
    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listarTodos() {
        return ResponseEntity.ok(servicoService.listarTodos());
    }

    /**
     * GET /api/v1/servicos/{id} — Busca serviço por ID.
     */
    @Operation(summary = "Buscar serviço por ID",
            description = "Retorna os dados de um serviço pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(
            @Parameter(description = "ID do serviço", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    /**
     * PUT /api/v1/servicos/{id} — Atualiza serviço existente.
     */
    @Operation(summary = "Atualizar serviço",
            description = "Atualiza os dados de um serviço existente pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Nome já pertence a outro serviço", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(
            @Parameter(description = "ID do serviço", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.ok(servicoService.atualizar(id, request));
    }

    /**
     * DELETE /api/v1/servicos/{id} — Remove serviço.
     */
    @Operation(summary = "Remover serviço",
            description = "Remove permanentemente um serviço pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do serviço", required = true)
            @PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}


