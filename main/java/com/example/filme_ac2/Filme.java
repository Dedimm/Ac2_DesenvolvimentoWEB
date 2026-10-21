package com.example.filme_ac2;

import com.google.firebase.firestore.Exclude;

public class Filme {
    private String id;
    private String nome;
    private String anoLancamento;
    private String genero;
    private String tipo;
    private boolean assistido;
    private double nota;

    public Filme() {} // Requisito para Firestore

    public Filme(String id, String nome, String anoLancamento, String genero,
                 String tipo, boolean assistido, double nota) {
        this.id = id;
        this.nome = nome;
        this.anoLancamento = anoLancamento;
        this.genero = genero;
        this.tipo = tipo;
        this.assistido = assistido;
        this.nota = nota;
    }

    @Exclude
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getAnoLancamento() { return anoLancamento; }
    public String getGenero() { return genero; }
    public String getTipo() { return tipo; }
    public boolean isAssistido() { return assistido; }
    public double getNota() { return nota; }

    @Exclude
    public void setId(String id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setAnoLancamento(String anoLancamento) { this.anoLancamento = anoLancamento; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setAssistido(boolean assistido) { this.assistido = assistido; }
    public void setNota(double nota) { this.nota = nota; }
}