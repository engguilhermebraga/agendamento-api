package com.guilhermebraga.agendamento_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;
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
@DisplayName("ServicoController — testes de integração com MockMvc")
class ServicoControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ServicoService servicoService;

    private ServicoRequest request;
    private ServicoResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        request = ServicoRequest.builder()
            .nome("Massagem Relaxante")
            .descricao("Massagem corporal de 60 minutos")
            .duracaoMinutos(60)
            .preco(new BigDecimal("120.00"))
            .build();

        response = ServicoResponse.builder()
            .id(1L)
            .nome("Massagem Relaxante")
            .descricao("Massagem corporal de 60 minutos")
            .duracaoMinutos(60)
            .preco(new BigDecimal("120.00"))
            .criadoEm(LocalDateTime.now())
            .atualizadoEm(LocalDateTime.now())
            .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/servicos — USER deve listar todos")
    void deveListarTodos() throws Exception {
        when(servicoService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/servicos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("Massagem Relaxante"))
            .andExpect(jsonPath("$[0].duracaoMinutos").value(60));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/servicos/{id} — USER deve buscar por ID")
    void deveBuscarPorId() throws Exception {
        when(servicoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/servicos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.preco").value(120.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/servicos/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoIdInexistente() throws Exception {
        when(servicoService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Serviço", 99L));

        mockMvc.perform(get("/api/v1/servicos/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/servicos — ADMIN deve criar serviço")
    void deveCriarComoAdmin() throws Exception {
        when(servicoService.criar(any(ServicoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Massagem Relaxante"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/servicos — USER recebe 403 Forbidden")
    void naoDevePermitirUserCriar() throws Exception {
        mockMvc.perform(post("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/servicos — deve retornar 400 com payload inválido")
    void deveRetornar400ComDadosInvalidos() throws Exception {
        ServicoRequest invalido = ServicoRequest.builder()
            .nome("")
            .duracaoMinutos(0)
            .preco(new BigDecimal("0.00"))
            .build();

        mockMvc.perform(post("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/servicos/{id} — ADMIN deve atualizar")
    void deveAtualizarComoAdmin() throws Exception {
        when(servicoService.atualizar(eq(1L), any(ServicoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/servicos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/servicos/{id} — ADMIN deve deletar")
    void deveDeletarComoAdmin() throws Exception {
        doNothing().when(servicoService).deletar(1L);

        mockMvc.perform(delete("/api/v1/servicos/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/servicos/{id} — USER recebe 403 Forbidden")
    void naoDevePermitirUserDeletar() throws Exception {
        mockMvc.perform(delete("/api/v1/servicos/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/servicos — sem autenticação retorna 401")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/servicos"))
            .andExpect(status().isUnauthorized());
    }
}
