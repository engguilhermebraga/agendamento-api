package com.guilhermebraga.agendamento_api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários do {@link JwtTokenProvider}: origem do segredo, validação
 * do tamanho da chave e expiração configurável.
 */
@DisplayName("JwtTokenProvider — segredo e expiração")
class JwtTokenProviderTest {

    private static final long UM_DIA_MS = 86_400_000L;
    /** 64 bytes: o mínimo para HS512. */
    private static final String SEGREDO_VALIDO =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private static MockEnvironment ambiente(String... perfis) {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(perfis);
        return env;
    }

    @Test
    @DisplayName("sem segredo fora de dev/test: não sobe")
    void semSegredoEmProducaoFalha() {
        assertThatThrownBy(() -> new JwtTokenProvider("", UM_DIA_MS, ambiente("prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("sem segredo e sem perfil ativo: não sobe")
    void semSegredoSemPerfilFalha() {
        assertThatThrownBy(() -> new JwtTokenProvider(null, UM_DIA_MS, ambiente()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("sem segredo com prod e dev juntos: não sobe")
    void semSegredoComProdEDevFalha() {
        assertThatThrownBy(() -> new JwtTokenProvider("", UM_DIA_MS, ambiente("prod", "dev")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("sem segredo em dev ou test: usa chave aleatória")
    void semSegredoEmDevUsaChaveAleatoria() {
        for (String perfil : new String[]{"dev", "test"}) {
            JwtTokenProvider provider = new JwtTokenProvider("  ", UM_DIA_MS, ambiente(perfil));
            String token = provider.generateToken("admin");
            assertThat(provider.validateToken(token)).isTrue();
            assertThat(provider.getUsuarioFromToken(token)).isEqualTo("admin");
        }
    }

    @Test
    @DisplayName("chaves aleatórias diferentes não aceitam o token uma da outra")
    void chavesAleatoriasSaoDiferentes() {
        JwtTokenProvider a = new JwtTokenProvider("", UM_DIA_MS, ambiente("dev"));
        JwtTokenProvider b = new JwtTokenProvider("", UM_DIA_MS, ambiente("dev"));
        assertThat(b.validateToken(a.generateToken("admin"))).isFalse();
    }

    @Test
    @DisplayName("segredo com menos de 64 bytes: não sobe")
    void segredoCurtoFalha() {
        String curto = SEGREDO_VALIDO.substring(1);
        assertThatThrownBy(() -> new JwtTokenProvider(curto, UM_DIA_MS, ambiente("dev")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("64 bytes");
    }

    @Test
    @DisplayName("segredo válido: gera e valida token com o usuário certo")
    void segredoValidoGeraEValidaToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SEGREDO_VALIDO, UM_DIA_MS, ambiente("prod"));
        String token = provider.generateToken("admin");

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsuarioFromToken(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("mesmo segredo em duas instâncias: tokens continuam válidos após restart")
    void mesmoSegredoEntreInstancias() {
        JwtTokenProvider a = new JwtTokenProvider(SEGREDO_VALIDO, UM_DIA_MS, ambiente("prod"));
        JwtTokenProvider b = new JwtTokenProvider(SEGREDO_VALIDO, UM_DIA_MS, ambiente("prod"));
        assertThat(b.validateToken(a.generateToken("admin"))).isTrue();
    }

    @Test
    @DisplayName("token assinado com outro segredo é rejeitado")
    void tokenDeOutroSegredoRejeitado() {
        String outro = SEGREDO_VALIDO.replace('0', 'x');
        JwtTokenProvider a = new JwtTokenProvider(SEGREDO_VALIDO, UM_DIA_MS, ambiente("prod"));
        JwtTokenProvider b = new JwtTokenProvider(outro, UM_DIA_MS, ambiente("prod"));
        assertThat(b.validateToken(a.generateToken("admin"))).isFalse();
    }

    @Test
    @DisplayName("expiração configurada aparece no token")
    void expiracaoConfiguravel() {
        long umaHora = 3_600_000L;
        JwtTokenProvider provider = new JwtTokenProvider(SEGREDO_VALIDO, umaHora, ambiente("prod"));

        long antes = System.currentTimeMillis();
        Date exp = provider.getExpirationFromToken(provider.generateToken("admin"));

        // O claim exp é gravado em segundos, então a tolerância é de 1 s
        assertThat(exp.getTime()).isBetween(antes + umaHora - 1_000, antes + umaHora + 1_000);
    }

    @Test
    @DisplayName("expiração zero ou negativa: não sobe")
    void expiracaoInvalidaFalha() {
        assertThatThrownBy(() -> new JwtTokenProvider(SEGREDO_VALIDO, 0, ambiente("prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.expiration-ms");
        assertThatThrownBy(() -> new JwtTokenProvider(SEGREDO_VALIDO, -1, ambiente("prod")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("expiração acima de 30 dias: não sobe (evita overflow da data)")
    void expiracaoAcimaDoLimiteFalha() {
        assertThatThrownBy(() -> new JwtTokenProvider(SEGREDO_VALIDO, Long.MAX_VALUE, ambiente("prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("30 dias");
        assertThatCode(() -> new JwtTokenProvider(
                SEGREDO_VALIDO, JwtTokenProvider.EXPIRACAO_MAXIMA_MS, ambiente("prod")))
                .doesNotThrowAnyException();
    }
}
