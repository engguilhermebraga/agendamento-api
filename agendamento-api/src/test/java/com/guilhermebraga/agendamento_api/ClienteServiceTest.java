package com.guilhermebraga.agendamento_api.service;

import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.dto.response.ClienteResponse;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.ClienteMapper;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários da camada de serviço de Cliente.
 * <p>
 * Usa JUnit 5 + Mockito para isolar o Service dos seus colaboradores
 * (Repository e Mapper), testando apenas a lógica de negócio.
 *
 * Convenção dos nomes: deve[Resultado]Quando[Condição]
 *
 * @author Guilherme Braga
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService — testes unitários")
class ClienteServiceTest {

    // ----------------------------------------------------------------
    // Colaboradores mockados (não são instâncias reais)
    // ----------------------------------------------------------------
    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    // Service com os mocks injetados automaticamente pelo Mockito
    @InjectMocks
    private ClienteService clienteService;

    // ----------------------------------------------------------------
    // Dados de teste reutilizáveis
    // ----------------------------------------------------------------
    private ClienteRequest requestValido;
    private Cliente clienteSalvo;
    private ClienteResponse responseEsperado;

    @BeforeEach
    void setUp() {
        // Monta os objetos de teste antes de cada @Test
        requestValido = new ClienteRequest(
                "João da Silva",
                "joao@email.com",
                "98988887777",
                "12345678900"
        );

        clienteSalvo = new Cliente();
        clienteSalvo.setId(1L);
        clienteSalvo.setNome("João da Silva");
        clienteSalvo.setEmail("joao@email.com");
        clienteSalvo.setTelefone("98988887777");
        clienteSalvo.setCpf("12345678900");
        clienteSalvo.setCriadoEm(LocalDateTime.now());
        clienteSalvo.setAtualizadoEm(LocalDateTime.now());

        responseEsperado = ClienteResponse.builder()
                .id(1L)
                .nome("João da Silva")
                .email("joao@email.com")
                .telefone("98988887777")
                .cpf("12345678900")
                .criadoEm(clienteSalvo.getCriadoEm())
                .atualizadoEm(clienteSalvo.getAtualizadoEm())
                .build();
    }

    // ================================================================
    // CRIAR
    // ================================================================

    @Test
    @DisplayName("deve criar cliente com dados válidos")
    void deveCriarClienteComSucesso() {
        // Arrange — nenhum cliente com e-mail ou CPF existente
        when(clienteRepository.findByEmail(requestValido.getEmail()))
                .thenReturn(Optional.empty());
        when(clienteRepository.findByCpf(requestValido.getCpf()))
                .thenReturn(Optional.empty());
        when(clienteMapper.toEntity(requestValido))
                .thenReturn(clienteSalvo);
        when(clienteRepository.save(clienteSalvo))
                .thenReturn(clienteSalvo);
        when(clienteMapper.toResponse(clienteSalvo))
                .thenReturn(responseEsperado);

        // Act
        ClienteResponse resultado = clienteService.criar(requestValido);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("João da Silva");
        assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
        assertThat(resultado.getId()).isEqualTo(1L);

        // Verifica que o save foi chamado exatamente uma vez
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("deve lançar BusinessException quando e-mail já estiver cadastrado")
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        // Arrange — simula e-mail já existente
        when(clienteRepository.findByEmail(requestValido.getEmail()))
                .thenReturn(Optional.of(clienteSalvo));

        // Act + Assert — verifica que a exceção é lançada com a mensagem correta
        assertThatThrownBy(() -> clienteService.criar(requestValido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(requestValido.getEmail());

        // Verifica que o save NUNCA foi chamado
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar BusinessException quando CPF já estiver cadastrado")
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        // Arrange — e-mail livre, mas CPF duplicado
        when(clienteRepository.findByEmail(requestValido.getEmail()))
                .thenReturn(Optional.empty());
        when(clienteRepository.findByCpf(requestValido.getCpf()))
                .thenReturn(Optional.of(clienteSalvo));

        // Act + Assert
        assertThatThrownBy(() -> clienteService.criar(requestValido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(requestValido.getCpf());

        verify(clienteRepository, never()).save(any());
    }

    // ================================================================
    // LISTAR TODOS
    // ================================================================

    @Test
    @DisplayName("deve retornar lista de clientes cadastrados")
    void deveListarTodosOsClientes() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(List.of(clienteSalvo));
        when(clienteMapper.toResponse(clienteSalvo)).thenReturn(responseEsperado);

        // Act
        List<ClienteResponse> resultado = clienteService.listarTodos();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("João da Silva");
    }

    @Test
    @DisplayName("deve retornar lista vazia quando não houver clientes")
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<ClienteResponse> resultado = clienteService.listarTodos();

        assertThat(resultado).isEmpty();
    }

    // ================================================================
    // BUSCAR POR ID
    // ================================================================

    @Test
    @DisplayName("deve retornar cliente quando o ID existir")
    void deveBuscarClientePorIdComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));
        when(clienteMapper.toResponse(clienteSalvo)).thenReturn(responseEsperado);

        ClienteResponse resultado = clienteService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException quando o ID não existir")
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ================================================================
    // ATUALIZAR
    // ================================================================

    @Test
    @DisplayName("deve atualizar cliente sem conflito de e-mail ou CPF")
    void deveAtualizarClienteComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));
        when(clienteRepository.existsByEmailAndIdNot(requestValido.getEmail(), 1L))
                .thenReturn(false);
        when(clienteRepository.existsByCpfAndIdNot(requestValido.getCpf(), 1L))
                .thenReturn(false);
        when(clienteRepository.save(clienteSalvo)).thenReturn(clienteSalvo);
        when(clienteMapper.toResponse(clienteSalvo)).thenReturn(responseEsperado);

        ClienteResponse resultado = clienteService.atualizar(1L, requestValido);

        assertThat(resultado).isNotNull();
        verify(clienteMapper, times(1)).updateEntityFromRequest(eq(requestValido), eq(clienteSalvo));
        verify(clienteRepository, times(1)).save(clienteSalvo);
    }

    @Test
    @DisplayName("deve lançar BusinessException ao atualizar com e-mail já usado por outro cliente")
    void deveLancarExcecaoAoAtualizarComEmailDuplicado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));
        when(clienteRepository.existsByEmailAndIdNot(requestValido.getEmail(), 1L))
                .thenReturn(true); // outro cliente já usa este e-mail

        assertThatThrownBy(() -> clienteService.atualizar(1L, requestValido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(requestValido.getEmail());

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar BusinessException ao atualizar com CPF já usado por outro cliente")
    void deveLancarExcecaoAoAtualizarComCpfDuplicado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));
        when(clienteRepository.existsByEmailAndIdNot(requestValido.getEmail(), 1L))
                .thenReturn(false);
        when(clienteRepository.existsByCpfAndIdNot(requestValido.getCpf(), 1L))
                .thenReturn(true); // outro cliente já usa este CPF

        assertThatThrownBy(() -> clienteService.atualizar(1L, requestValido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(requestValido.getCpf());

        verify(clienteRepository, never()).save(any());
    }

    // ================================================================
    // DELETAR
    // ================================================================

    @Test
    @DisplayName("deve deletar cliente existente sem lançar exceção")
    void deveDeletarClienteComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));

        // Act — não deve lançar nenhuma exceção
        assertThatCode(() -> clienteService.deletar(1L))
                .doesNotThrowAnyException();

        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException ao deletar ID inexistente")
    void deveLancarExcecaoAoDeletarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteRepository, never()).deleteById(any());
    }
}
