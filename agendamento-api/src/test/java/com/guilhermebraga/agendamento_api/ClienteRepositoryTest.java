package com.guilhermebraga.agendamento_api;

import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.repository.AgendamentoRepository;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes da camada de Repository do Cliente.
 *
 * Cobre o contrato esperado do {@link ClienteRepository}:
 * — métodos herdados do JpaRepository (findAll, findById, count, save, delete)
 * — queries derivadas (findByEmail, findByCpf)
 * — checagens de existência usadas em validação de duplicidade
 *   (existsByEmailAndIdNot, existsByCpfAndIdNot)
 *
 * Estratégia: {@code @SpringBootTest + @Transactional} — a transação envolve
 * o @BeforeEach + o método de teste e é revertida no final, garantindo
 * isolamento entre métodos. O @BeforeEach também limpa explicitamente
 * quaisquer dados comitados por outras classes (ex.: AgendamentoIntegrationTest,
 * que não é @Transactional), evitando contaminação no banco H2 compartilhado.
 *
 * @author Guilherme Braga
 */
@SpringBootTest
@Transactional
@DisplayName("ClienteRepository — testes da camada de persistência")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    private Cliente joao;
    private Cliente maria;

    @BeforeEach
    void setUp() {
        // Remove dados comitados por outras classes antes de criar os fixtures.
        // A ordem importa: agendamentos referenciam clientes (FK), então
        // devem ser removidos primeiro.
        agendamentoRepository.deleteAll();
        clienteRepository.deleteAll();

        joao = clienteRepository.save(Cliente.builder()
                .nome("João da Silva")
                .email("joao@email.com")
                .telefone("98988887777")
                .cpf("12345678900")
                .build());

        maria = clienteRepository.save(Cliente.builder()
                .nome("Maria Souza")
                .email("maria@email.com")
                .telefone("98999998888")
                .cpf("98765432100")
                .build());
    }

    // ================================================================
    // findAll() — herdado de JpaRepository
    // ================================================================

    @Test
    @DisplayName("findAll — retorna todos os clientes persistidos")
    void findAllDeveRetornarTodosOsClientes() {
        List<Cliente> resultado = clienteRepository.findAll();

        assertThat(resultado)
                .hasSize(2)
                .extracting(Cliente::getEmail)
                .containsExactlyInAnyOrder("joao@email.com", "maria@email.com");
    }

    @Test
    @DisplayName("findAll — retorna lista vazia quando não há clientes")
    void findAllDeveRetornarListaVaziaSemDados() {
        clienteRepository.deleteAll();

        List<Cliente> resultado = clienteRepository.findAll();

        assertThat(resultado).isEmpty();
    }

    // ================================================================
    // findById() — herdado de JpaRepository
    // ================================================================

    @Test
    @DisplayName("findById — retorna cliente quando ID existe")
    void findByIdDeveRetornarClienteQuandoExiste() {
        Optional<Cliente> resultado = clienteRepository.findById(joao.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("João da Silva");
    }

    @Test
    @DisplayName("findById — retorna Optional vazio quando ID não existe")
    void findByIdDeveRetornarVazioQuandoNaoExiste() {
        Optional<Cliente> resultado = clienteRepository.findById(99999L);

        assertThat(resultado).isEmpty();
    }

    // ================================================================
    // findByEmail()
    // ================================================================

    @Test
    @DisplayName("findByEmail — retorna cliente quando e-mail existe")
    void findByEmailDeveRetornarClienteQuandoExiste() {
        Optional<Cliente> resultado = clienteRepository.findByEmail("joao@email.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCpf()).isEqualTo("12345678900");
    }

    @Test
    @DisplayName("findByEmail — retorna Optional vazio quando e-mail não cadastrado")
    void findByEmailDeveRetornarVazioQuandoNaoExiste() {
        Optional<Cliente> resultado = clienteRepository.findByEmail("inexistente@email.com");

        assertThat(resultado).isEmpty();
    }

    // ================================================================
    // findByCpf()
    // ================================================================

    @Test
    @DisplayName("findByCpf — retorna cliente quando CPF existe")
    void findByCpfDeveRetornarClienteQuandoExiste() {
        Optional<Cliente> resultado = clienteRepository.findByCpf("98765432100");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Maria Souza");
    }

    @Test
    @DisplayName("findByCpf — retorna Optional vazio quando CPF não cadastrado")
    void findByCpfDeveRetornarVazioQuandoNaoExiste() {
        Optional<Cliente> resultado = clienteRepository.findByCpf("00000000000");

        assertThat(resultado).isEmpty();
    }

    // ================================================================
    // existsByEmailAndIdNot() — checagem de duplicidade na atualização
    // ================================================================

    @Test
    @DisplayName("existsByEmailAndIdNot — retorna true quando e-mail pertence a OUTRO cliente")
    void existsByEmailAndIdNotDeveRetornarTrueQuandoOutroClienteUsaEmail() {
        // tentando "atualizar" o joão com o e-mail da maria
        boolean existe = clienteRepository.existsByEmailAndIdNot("maria@email.com", joao.getId());

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByEmailAndIdNot — retorna false quando o próprio cliente é o dono do e-mail")
    void existsByEmailAndIdNotDeveRetornarFalseParaProprioCliente() {
        // joão atualizando seus dados sem mudar o e-mail
        boolean existe = clienteRepository.existsByEmailAndIdNot("joao@email.com", joao.getId());

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndIdNot — retorna false quando e-mail não existe no sistema")
    void existsByEmailAndIdNotDeveRetornarFalseQuandoEmailNovo() {
        boolean existe = clienteRepository.existsByEmailAndIdNot("novo@email.com", joao.getId());

        assertThat(existe).isFalse();
    }

    // ================================================================
    // existsByCpfAndIdNot() — checagem de duplicidade na atualização
    // ================================================================

    @Test
    @DisplayName("existsByCpfAndIdNot — retorna true quando CPF pertence a OUTRO cliente")
    void existsByCpfAndIdNotDeveRetornarTrueQuandoOutroClienteUsaCpf() {
        boolean existe = clienteRepository.existsByCpfAndIdNot("98765432100", joao.getId());

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByCpfAndIdNot — retorna false quando o próprio cliente é o dono do CPF")
    void existsByCpfAndIdNotDeveRetornarFalseParaProprioCliente() {
        boolean existe = clienteRepository.existsByCpfAndIdNot("12345678900", joao.getId());

        assertThat(existe).isFalse();
    }

    // ================================================================
    // count() e delete() — herdados de JpaRepository
    // ================================================================

    @Test
    @DisplayName("count — retorna o total de clientes persistidos")
    void countDeveRetornarTotalCorreto() {
        long total = clienteRepository.count();

        assertThat(total).isEqualTo(2L);
    }

    @Test
    @DisplayName("deleteById — remove cliente do banco")
    void deleteByIdDeveRemoverCliente() {
        clienteRepository.deleteById(joao.getId());

        assertThat(clienteRepository.findById(joao.getId())).isEmpty();
        assertThat(clienteRepository.count()).isEqualTo(1L);
    }

    // ================================================================
    // save() — INSERT e UPDATE
    // ================================================================

    @Test
    @DisplayName("save — atualiza cliente existente preservando ID")
    void saveDeveAtualizarClienteExistente() {
        joao.setTelefone("98911112222");
        Cliente atualizado = clienteRepository.save(joao);

        assertThat(atualizado.getId()).isEqualTo(joao.getId());
        assertThat(atualizado.getTelefone()).isEqualTo("98911112222");
    }

    @Test
    @DisplayName("save — preenche criadoEm e atualizadoEm via @PrePersist")
    void saveDevePreencherDatasAutomaticamente() {
        Cliente novo = clienteRepository.save(Cliente.builder()
                .nome("Pedro Costa")
                .email("pedro@email.com")
                .telefone("98955554444")
                .cpf("11122233344")
                .build());

        assertThat(novo.getCriadoEm()).isNotNull();
        assertThat(novo.getAtualizadoEm()).isNotNull();
    }
}
