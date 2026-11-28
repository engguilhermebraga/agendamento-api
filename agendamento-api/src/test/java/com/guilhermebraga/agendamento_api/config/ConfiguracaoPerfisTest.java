package com.guilhermebraga.agendamento_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * Garante a separação dos perfis nos arquivos de {@code src/main/resources}.
 * <p>
 * Os arquivos são lidos do disco porque, no classpath dos testes, o
 * {@code application.properties} de {@code src/test/resources} sobrepõe o principal.
 */
@DisplayName("Configuração — separação dos perfis dev e prod")
class ConfiguracaoPerfisTest {

    /** O Surefire define {@code basedir}; numa IDE, o diretório atual costuma ser o módulo. */
    private static final Path RESOURCES =
            Path.of(System.getProperty("basedir", "."), "src", "main", "resources");

    private static Properties carregar(String arquivo) throws IOException {
        Path caminho = RESOURCES.resolve(arquivo);
        assertThat(caminho).as("arquivo %s", arquivo).exists();
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    @Test
    @DisplayName("base: não ativa perfil nem define banco ou console H2")
    void baseNaoFixaPerfil() throws IOException {
        Properties base = carregar("application.properties");

        assertThat(base).doesNotContainKey("spring.profiles.active");
        // Sem perfil, vale o prod (exige DB_URL e JWT_SECRET) e não um H2 descartável
        assertThat(base.getProperty("spring.profiles.default")).isEqualTo("prod");
        assertThat(base).doesNotContainKey("spring.datasource.url");
        assertThat(base.getProperty("spring.h2.console.enabled", "false")).isEqualTo("false");
        assertThat(base.getProperty("spring.jpa.show-sql", "false")).isEqualTo("false");
    }

    @Test
    @DisplayName("dev: H2 em memória e console H2 ligado")
    void devUsaH2() throws IOException {
        Properties dev = carregar("application-dev.properties");

        assertThat(dev.getProperty("spring.datasource.url")).startsWith("jdbc:h2:mem:");
        assertThat(dev.getProperty("spring.h2.console.enabled")).isEqualTo("true");
    }

    @Test
    @DisplayName("prod: credenciais só por variável de ambiente e console H2 desligado")
    void prodSoComVariaveisDeAmbiente() throws IOException {
        Properties prod = carregar("application-prod.properties");

        assertThat(prod.getProperty("spring.datasource.url")).isEqualTo("${DB_URL}");
        assertThat(prod.getProperty("spring.datasource.username")).isEqualTo("${DB_USER}");
        assertThat(prod.getProperty("spring.datasource.password")).isEqualTo("${DB_PASSWORD}");
        assertThat(prod.getProperty("spring.h2.console.enabled")).isEqualTo("false");
        // Nunca create/create-drop em produção (apagaria os dados)
        assertThat(prod.getProperty("spring.jpa.hibernate.ddl-auto")).isIn("${JPA_DDL_AUTO:update}", "validate");
        assertThat(prod).doesNotContainKey("spring.profiles.active");
    }

    @Test
    @DisplayName("logback: SQL e valores dos parâmetros só são logados no perfil dev")
    void logDeSqlSoEmDev() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(RESOURCES.resolve("logback-spring.xml").toFile());

        NodeList loggers = doc.getElementsByTagName("logger");
        int encontrados = 0;
        for (int i = 0; i < loggers.getLength(); i++) {
            Element logger = (Element) loggers.item(i);
            String nome = logger.getAttribute("name");
            if (nome.equals("org.hibernate.SQL") || nome.startsWith("org.hibernate.orm.jdbc.bind")) {
                encontrados++;
                Node pai = logger.getParentNode();
                assertThat(pai.getNodeName()).as("logger %s", nome).isEqualTo("springProfile");
                assertThat(((Element) pai).getAttribute("name")).as("perfil do logger %s", nome).isEqualTo("dev");
            }
        }
        assertThat(encontrados).isEqualTo(2);
    }
}
