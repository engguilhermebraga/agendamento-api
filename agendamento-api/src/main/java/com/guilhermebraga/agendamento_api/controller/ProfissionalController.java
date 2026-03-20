package com.guilhermebraga.agendamento_api.controller;

import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
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
 * Controller REST da entidade Profissional.
 * Base URL: /api/v1/profissionais
 *
 * @author Guilherme Braga
 */
@RestController
@RequestMapping("/api/v1/profissionais")
@RequiredArgsConstructor
@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    /**
     * POST /api/v1/profissionais — Cadastra novo profissional.
     */
    @Operation(summary = "Cadastrar novo profissional",
            description = "Cria um novo profissional validando unicidade do e-mail.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profissional criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfissionalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProfissionalResponse> criar(
            @Valid @RequestBody ProfissionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profissionalService.criar(request));
    }

    /**
     * GET /api/v1/profissionais — Lista todos os profissionais.
     */
    @Operation(summary = "Listar todos os profissionais",
            description = "Retorna a lista completa de profissionais cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = ProfissionalResponse.class)))
    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listarTodos() {
        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    /**
     * GET /api/v1/profissionais/{id} — Busca profissional por ID.
     */
    @Operation(summary = "Buscar profissional por ID",
            description = "Retorna os dados de um profissional pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissional encontrado",
                    content = @Content(schema = @Schema(implementation = ProfissionalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscarPorId(
            @Parameter(description = "ID do profissional", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.buscarPorId(id));
    }

    /**
     * PUT /api/v1/profissionais/{id} — Atualiza profissional existente.
     */
    @Operation(summary = "Atualizar profissional",
            description = "Atualiza os dados de um profissional existente pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissional atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfissionalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já pertence a outro profissional", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> atualizar(
            @Parameter(description = "ID do profissional", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProfissionalRequest request) {
        return ResponseEntity.ok(profissionalService.atualizar(id, request));
    }

    /**
     * DELETE /api/v1/profissionais/{id} — Remove profissional.
     */
    @Operation(summary = "Remover profissional",
            description = "Remove permanentemente um profissional pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profissional removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do profissional", required = true)
            @PathVariable Long id) {
        profissionalService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}