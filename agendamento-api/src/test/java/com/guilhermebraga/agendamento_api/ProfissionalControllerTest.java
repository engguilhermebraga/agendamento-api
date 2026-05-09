package com.guilhermebraga.agendamento_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProfissionalController — testes de integração com MockMvc")
class ProfissionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfissionalService profissionalService;

    private ProfissionalRequest request;
    private ProfissionalResponse response;

    @BeforeEach
    void setUp() {
        request = ProfissionalRequest.builder()
            .nome("Ana Souza")
            .especialidade("Fisioterapeuta")
            .email("ana@email.com")
            .telefone("98999876543")
            .build();

        response = ProfissionalResponse.builder()
            .id(1L)
            .nome("Ana Souza")
            .especialidade("Fisioterapeuta")
            .email("ana@email.com")
            .telefone("98999876543")
            .criadoEm(LocalDateTime.now())
            .atualizadoEm(LocalDateTime.now())
            .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/profissionais — USER deve listar todos")
    void deveListarTodos() throws Exception {
        when(profissionalService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/profissionais"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("Ana Souza"))
            .andExpect(jsonPath("$[0].especialidade").value("Fisioterapeuta"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/profissionais/{id} — USER deve buscar por ID")
    void deveBuscarPorId() throws Exception {
        when(profissionalService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/profissionais/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/profissionais/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoIdInexistente() throws Exception {
        when(profissionalService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Profissional", 99L));

        mockMvc.perform(get("/api/v1/profissionais/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/profissionais — ADMIN deve criar profissional")
    void deveCriarComoAdmin() throws Exception {
        when(profissionalService.criar(any(ProfissionalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/profissionais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.especialidade").value("Fisioterapeuta"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/profissionais — USER recebe 403 Forbidden")
    void naoDevePermitirUserCriar() throws Exception {
        mockMvc.perform(post("/api/v1/profissionais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/profissionais — deve retornar 400 com payload inválido")
    void deveRetornar400ComDadosInvalidos() throws Exception {
        ProfissionalRequest invalido = ProfissionalRequest.builder()
            .nome("")
            .especialidade("")
            .email("invalido")
            .telefone("1")
            .build();

        mockMvc.perform(post("/api/v1/profissionais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/profissionais/{id} — ADMIN deve atualizar")
    void deveAtualizarComoAdmin() throws Exception {
        when(profissionalService.atualizar(eq(1L), any(ProfissionalRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/profissionais/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/profissionais/{id} — ADMIN deve deletar")
    void deveDeletarComoAdmin() throws Exception {
        doNothing().when(profissionalService).deletar(1L);

        mockMvc.perform(delete("/api/v1/profissionais/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/profissionais/{id} — USER recebe 403 Forbidden")
    void naoDevePermitirUserDeletar() throws Exception {
        mockMvc.perform(delete("/api/v1/profissionais/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/profissionais — sem autenticação retorna 401")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/profissionais"))
            .andExpect(status().isUnauthorized());
    }
}
