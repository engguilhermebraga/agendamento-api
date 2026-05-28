package com.guilhermebraga.agendamento_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.repository.AgendamentoRepository;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import com.guilhermebraga.agendamento_api.repository.ProfissionalRepository;
import com.guilhermebraga.agendamento_api.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração end-to-end com toda a stack:
 * Controller → Service → Repository → H2 (banco real em memória).
 *
 * Não usa mocks dos services/repositories — apenas a autenticação é simulada
 * via @WithMockUser para não exigir credenciais reais em cada requisição.
 *
 * Cenários cobertos:
 * - Fluxo completo: cliente → profissional → serviço → agendamento → CONFIRMADO → CONCLUIDO
 * - Detecção de conflito de horário (agendamentos sobrepostos para o mesmo profissional)
 * - Aceite de agendamentos em horários separados
 * - Validação da máquina de estados de status
 *
 * @author Guilherme Braga
 */
@SpringBootTest
@WithMockUser(roles = "ADMIN")
@DisplayName("Integração — fluxo completo de agendamento (toda a stack)")
class AgendamentoIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    private LocalDateTime amanhaAs10h;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Ordem de limpeza respeita FKs: agendamento depende de cliente/profissional/servico
        agendamentoRepository.deleteAll();
        clienteRepository.deleteAll();
        profissionalRepository.deleteAll();
        servicoRepository.deleteAll();

        amanhaAs10h = LocalDateTime.now()
                .plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    // ================================================================
    // FLUXO COMPLETO: criar cliente → agendamento → mudar status
    // ================================================================

    @Test
    @DisplayName("fluxo completo: cria cliente → profissional → serviço → agendamento → CONFIRMADO → CONCLUIDO")
    void fluxoCompletoCriarClienteAgendamentoMudarStatus() throws Exception {
        // 1. Cria cliente via POST /api/v1/clientes
        Long clienteId = criarCliente("João da Silva", "joao@email.com", "98988887777", "12345678900");
        assertThat(clienteRepository.findById(clienteId)).isPresent();

        // 2. Cria profissional via POST /api/v1/profissionais
        Long profissionalId = criarProfissional("Ana Souza", "Fisioterapeuta", "ana@email.com", "98999876543");
        assertThat(profissionalRepository.findById(profissionalId)).isPresent();

        // 3. Cria serviço de 60 minutos via POST /api/v1/servicos
        Long servicoId = criarServico("Massagem Relaxante", "Sessão de 60 min", 60, new BigDecimal("120.00"));
        assertThat(servicoRepository.findById(servicoId)).isPresent();

        // 4. Cria agendamento via POST /api/v1/agendamentos
        Long agendamentoId = criarAgendamento(clienteId, profissionalId, servicoId, amanhaAs10h);

        // Confere persistência e status inicial AGENDADO
        mockMvc.perform(get("/api/v1/agendamentos/" + agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.clienteNome").value("João da Silva"))
                .andExpect(jsonPath("$.profissionalNome").value("Ana Souza"))
                .andExpect(jsonPath("$.servicoNome").value("Massagem Relaxante"))
                .andExpect(jsonPath("$.servicoDuracaoMinutos").value(60));

        // 5. AGENDADO → CONFIRMADO
        mockMvc.perform(patch("/api/v1/agendamentos/" + agendamentoId + "/status")
                        .param("status", "CONFIRMADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        // 6. CONFIRMADO → CONCLUIDO
        mockMvc.perform(patch("/api/v1/agendamentos/" + agendamentoId + "/status")
                        .param("status", "CONCLUIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        // 7. CONCLUIDO é estado final — qualquer mudança deve falhar com 409
        mockMvc.perform(patch("/api/v1/agendamentos/" + agendamentoId + "/status")
                        .param("status", "CANCELADO"))
                .andExpect(status().isConflict());

        // Verifica persistência final no banco
        var persistido = agendamentoRepository.findById(agendamentoId).orElseThrow();
        assertThat(persistido.getStatus().name()).isEqualTo("CONCLUIDO");
    }

    @Test
    @DisplayName("máquina de estados: não deve permitir pular AGENDADO → CONCLUIDO direto")
    void naoDevePularEstadosDiretamente() throws Exception {
        Long clienteId = criarCliente("Teste Cliente", "teste@email.com", "98988887777", "11122233344");
        Long profissionalId = criarProfissional("Profissional X", "Especialidade X", "profi@email.com", "98999876543");
        Long servicoId = criarServico("Serviço X", "desc", 30, new BigDecimal("50.00"));
        Long agendamentoId = criarAgendamento(clienteId, profissionalId, servicoId, amanhaAs10h);

        // AGENDADO → CONCLUIDO (pulando CONFIRMADO) deve falhar
        mockMvc.perform(patch("/api/v1/agendamentos/" + agendamentoId + "/status")
                        .param("status", "CONCLUIDO"))
                .andExpect(status().isConflict());

        // Estado deve permanecer AGENDADO no banco
        var persistido = agendamentoRepository.findById(agendamentoId).orElseThrow();
        assertThat(persistido.getStatus().name()).isEqualTo("AGENDADO");
    }

    @Test
    @DisplayName("AGENDADO → CANCELADO via PATCH: deve cancelar com sucesso")
    void deveCancelarAgendamentoAgendado() throws Exception {
        Long clienteId = criarCliente("Cancela Cliente", "cancela@email.com", "98988887777", "11122233344");
        Long profissionalId = criarProfissional("Profissional C", "Especialidade", "profi@email.com", "98999876543");
        Long servicoId = criarServico("Serviço Cancelar", "desc", 30, new BigDecimal("50.00"));
        Long agendamentoId = criarAgendamento(clienteId, profissionalId, servicoId, amanhaAs10h);

        mockMvc.perform(patch("/api/v1/agendamentos/" + agendamentoId + "/status")
                        .param("status", "CANCELADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        var persistido = agendamentoRepository.findById(agendamentoId).orElseThrow();
        assertThat(persistido.getStatus().name()).isEqualTo("CANCELADO");
    }

    // ================================================================
    // CONFLITO DE HORÁRIO END-TO-END
    // ================================================================

    @Test
    @DisplayName("conflito de horário: dois agendamentos sobrepostos no mesmo profissional → 409")
    void deveDetectarConflitoDeHorarioEndToEnd() throws Exception {
        Long cliente1Id = criarCliente("Cliente Um", "c1@email.com", "98988880001", "11111111111");
        Long cliente2Id = criarCliente("Cliente Dois", "c2@email.com", "98988880002", "22222222222");
        Long profissionalId = criarProfissional("Único Profi", "Fisio", "profi@email.com", "98999876543");
        Long servicoId = criarServico("Massagem 60min", "1h", 60, new BigDecimal("120.00"));

        // Primeiro agendamento: 10:00 - 11:00
        Long primeiroId = criarAgendamento(cliente1Id, profissionalId, servicoId, amanhaAs10h);
        assertThat(primeiroId).isPositive();

        // Segundo agendamento: 10:30 - 11:30 (sobrepõe os 30 min finais do primeiro)
        AgendamentoRequest sobreposto = AgendamentoRequest.builder()
                .clienteId(cliente2Id)
                .profissionalId(profissionalId)
                .servicoId(servicoId)
                .dataHora(amanhaAs10h.plusMinutes(30))
                .build();

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sobreposto)))
                .andExpect(status().isConflict());

        // Apenas um agendamento foi persistido — o segundo foi rejeitado
        assertThat(agendamentoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("conflito de horário: mesmo dataHora exato deve ser rejeitado")
    void deveDetectarConflitoComMesmoHorarioExato() throws Exception {
        Long cliente1Id = criarCliente("Cliente Um", "c1@email.com", "98988880001", "11111111111");
        Long cliente2Id = criarCliente("Cliente Dois", "c2@email.com", "98988880002", "22222222222");
        Long profissionalId = criarProfissional("Profissional Z", "Fisio", "profi@email.com", "98999876543");
        Long servicoId = criarServico("Servico Z", "desc", 60, new BigDecimal("100.00"));

        criarAgendamento(cliente1Id, profissionalId, servicoId, amanhaAs10h);

        AgendamentoRequest mesmoHorario = AgendamentoRequest.builder()
                .clienteId(cliente2Id)
                .profissionalId(profissionalId)
                .servicoId(servicoId)
                .dataHora(amanhaAs10h)
                .build();

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mesmoHorario)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("não há conflito quando agendamentos estão em horários separados")
    void deveAceitarAgendamentosEmHorariosSeparados() throws Exception {
        Long cliente1Id = criarCliente("Cliente Sep1", "c1@email.com", "98988880001", "11111111111");
        Long cliente2Id = criarCliente("Cliente Sep2", "c2@email.com", "98988880002", "22222222222");
        Long profissionalId = criarProfissional("Profissional Sep", "Fisio", "profi@email.com", "98999876543");
        Long servicoId = criarServico("Servico Sep", "desc", 60, new BigDecimal("100.00"));

        // 10:00 - 11:00
        criarAgendamento(cliente1Id, profissionalId, servicoId, amanhaAs10h);
        // 12:00 - 13:00 (sem sobreposição)
        criarAgendamento(cliente2Id, profissionalId, servicoId, amanhaAs10h.plusHours(2));

        assertThat(agendamentoRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("não há conflito quando profissionais são diferentes no mesmo horário")
    void deveAceitarMesmoHorarioParaProfissionaisDiferentes() throws Exception {
        Long clienteId = criarCliente("Cliente", "cli@email.com", "98988880001", "11111111111");
        Long profi1Id = criarProfissional("Profi A", "Fisio", "a@email.com", "98999876541");
        Long profi2Id = criarProfissional("Profi B", "Massagem", "b@email.com", "98999876542");
        Long servicoId = criarServico("Servico", "desc", 60, new BigDecimal("100.00"));

        // Mesmo cliente e horário, mas profissionais diferentes — permitido
        criarAgendamento(clienteId, profi1Id, servicoId, amanhaAs10h);
        criarAgendamento(clienteId, profi2Id, servicoId, amanhaAs10h);

        assertThat(agendamentoRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("conflito após cancelamento: cancelar libera horário para novo agendamento")
    void deveLiberarHorarioAposCancelamento() throws Exception {
        Long cliente1Id = criarCliente("Cliente Lib1", "c1@email.com", "98988880001", "11111111111");
        Long cliente2Id = criarCliente("Cliente Lib2", "c2@email.com", "98988880002", "22222222222");
        Long profissionalId = criarProfissional("Profissional Lib", "Fisio", "profi@email.com", "98999876543");
        Long servicoId = criarServico("Servico Lib", "desc", 60, new BigDecimal("100.00"));

        Long primeiroId = criarAgendamento(cliente1Id, profissionalId, servicoId, amanhaAs10h);

        // Cancela o primeiro
        mockMvc.perform(patch("/api/v1/agendamentos/" + primeiroId + "/status")
                        .param("status", "CANCELADO"))
                .andExpect(status().isOk());

        // Novo agendamento no mesmo horário deve ser aceito (CANCELADO é ignorado nos conflitos)
        Long segundoId = criarAgendamento(cliente2Id, profissionalId, servicoId, amanhaAs10h);
        assertThat(segundoId).isNotEqualTo(primeiroId);
    }

    // ================================================================
    // HELPERS — POST e leitura do ID retornado
    // ================================================================

    private Long criarCliente(String nome, String email, String telefone, String cpf) throws Exception {
        ClienteRequest req = ClienteRequest.builder()
                .nome(nome).email(email).telefone(telefone).cpf(cpf).build();

        MvcResult result = mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long criarProfissional(String nome, String especialidade, String email, String telefone) throws Exception {
        ProfissionalRequest req = ProfissionalRequest.builder()
                .nome(nome).especialidade(especialidade).email(email).telefone(telefone).build();

        MvcResult result = mockMvc.perform(post("/api/v1/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long criarServico(String nome, String descricao, Integer duracao, BigDecimal preco) throws Exception {
        ServicoRequest req = ServicoRequest.builder()
                .nome(nome).descricao(descricao).duracaoMinutos(duracao).preco(preco).build();

        MvcResult result = mockMvc.perform(post("/api/v1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long criarAgendamento(Long clienteId, Long profissionalId, Long servicoId, LocalDateTime dataHora) throws Exception {
        AgendamentoRequest req = AgendamentoRequest.builder()
                .clienteId(clienteId)
                .profissionalId(profissionalId)
                .servicoId(servicoId)
                .dataHora(dataHora)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
