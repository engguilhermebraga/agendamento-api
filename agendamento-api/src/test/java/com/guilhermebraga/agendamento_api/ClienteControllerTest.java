package com.guilhermebraga.agendamento_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.dto.response.ClienteResponse;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.ClienteService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("ClienteController — testes de integração com MockMvc")
class ClienteControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ClienteService clienteService;

    private ClienteRequest request;
    private ClienteResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        request = ClienteRequest.builder()
            .nome("João da Silva")
            .email("joao@email.com")
            .telefone("98988887777")
            .cpf("12345678900")
            .build();

        response = ClienteResponse.builder()
            .id(1L)
            .nome("João da Silva")
            .email("joao@email.com")
            .telefone("98988887777")
            .cpf("12345678900")
            .criadoEm(LocalDateTime.now())
            .atualizadoEm(LocalDateTime.now())
            .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/clientes — USER deve listar todos")
    void deveListarTodos() throws Exception {
        when(clienteService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nome").value("João da Silva"))
            .andExpect(jsonPath("$[0].email").value("joao@email.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/clientes/{id} — USER deve buscar por ID")
    void deveBuscarPorId() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/clientes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.cpf").value("12345678900"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/clientes/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoIdInexistente() throws Exception {
        when(clienteService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Cliente", 99L));

        mockMvc.perform(get("/api/v1/clientes/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/clientes — ADMIN deve criar cliente")
    void deveCriarComoAdmin() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("João da Silva"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/clientes — USER recebe 403 Forbidden")
    void naoDevePermitirUserCriar() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/clientes — deve retornar 400 com payload inválido")
    void deveRetornar400ComDadosInvalidos() throws Exception {
        ClienteRequest invalido = ClienteRequest.builder()
            .nome("")
            .email("nao-e-email")
            .telefone("123")
            .cpf("abc")
            .build();

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/clientes/{id} — ADMIN deve atualizar")
    void deveAtualizarComoAdmin() throws Exception {
        when(clienteService.atualizar(eq(1L), any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/clientes/{id} — ADMIN deve deletar")
    void deveDeletarComoAdmin() throws Exception {
        doNothing().when(clienteService).deletar(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/clientes/{id} — USER recebe 403 Forbidden")
    void naoDevePermitirUserDeletar() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/clientes/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        doThrow(new ResourceNotFoundException("Cliente", 99L))
            .when(clienteService).deletar(99L);

        mockMvc.perform(delete("/api/v1/clientes/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/clientes — sem autenticação retorna 401")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
            .andExpect(status().isUnauthorized());
    }
}
