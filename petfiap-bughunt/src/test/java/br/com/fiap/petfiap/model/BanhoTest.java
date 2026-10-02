package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Testes unitarios do model: sem banco, sem Spring (Aula 15).
public class BanhoTest {

    private Banho banhoDoRex() {
        return new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    public void deveAcumular20PontosDeFidelidade() {
        // Act
        int pontos = banhoDoRex().calcularPontosFidelidade();

        // Assert
        assertEquals(20, pontos);
    }

    @Test
    public void deveDurar45Minutos() {
        // Act
        int duracao = banhoDoRex().getDuracaoMinutos();

        // Assert
        assertEquals(45, duracao);
    }

    @Test
    public void deveCustar80ReaisParaPorteMedio() {
        Banho banhoMedio = new Banho(2, "Bob", "MEDIO", "Carlos", LocalDateTime.now());
        assertEquals(80.0, banhoMedio.calcularPreco(), 0.001);
    }

    @Test
    public void deveCustar100ReaisParaPorteGrande() {
        Banho banhoGrande = new Banho(3, "Thor", "GRANDE", "Mariana", LocalDateTime.now());
        assertEquals(100.0, banhoGrande.calcularPreco(), 0.001);
    }
}