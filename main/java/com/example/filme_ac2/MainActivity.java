package com.example.filme_ac2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText editTextNome, editTextNota, editTextLancamento, editTextBusca;
    private RecyclerView recycler;
    private final List<Filme> listaCompleta = new ArrayList<>(); // tudo do Firestore
    private final List<Filme> listaFilme = new ArrayList<>();    // o que aparece na tela
    private FilmeAdapter adapter;
    private Spinner spinnerTipo, spinnerGenero;
    private CheckBox checkBox;
    private Button button;
    private Filme filmeEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        editTextNome = findViewById(R.id.editTextNome);
        editTextNota = findViewById(R.id.editTextNota);
        editTextLancamento = findViewById(R.id.editTextLancamento);
        editTextBusca = findViewById(R.id.editTextBusca);
        recycler = findViewById(R.id.recycler);
        spinnerTipo = findViewById(R.id.spinner);
        spinnerGenero = findViewById(R.id.spinner2);
        checkBox = findViewById(R.id.checkBox);
        button = findViewById(R.id.button);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilmeAdapter(listaFilme);
        recycler.setAdapter(adapter);

        String[] tipos = {"Selecione o tipo", "Filme", "Série", "Anime", "Documentário"};
        String[] generos = {"Selecione o gênero", "Ação", "Comédia", "Drama", "Terror", "Ficção", "Romance", "Animação"};
        spinnerTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipos));
        spinnerGenero.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, generos));


        adapter.setOnItemClickListener(filme -> {
            editTextNome.setText(filme.getNome());
            editTextLancamento.setText(filme.getAnoLancamento());
            editTextNota.setText(String.valueOf(filme.getNota()));
            selecionarSpinner(spinnerTipo, filme.getTipo());
            selecionarSpinner(spinnerGenero, filme.getGenero());
            checkBox.setChecked(filme.isAssistido());
            filmeEditando = filme;
            button.setText("Atualizar");
        });


        adapter.setOnItemLongClickListener(this::confirmarExclusao);


        editTextBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                aplicarFiltro();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        button.setOnClickListener(v -> salvarFilme());

        carregarFilmes();
    }

    private void salvarFilme() {
        String nome = editTextNome.getText().toString().trim();
        String ano = editTextLancamento.getText().toString().trim();
        String notaTexto = editTextNota.getText().toString().trim().replace(",", ".");
        boolean assistido = checkBox.isChecked();


        if (nome.isEmpty()) {
            editTextNome.setError("Informe o nome");
            editTextNome.requestFocus();
            return;
        }
        if (spinnerTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o tipo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o gênero", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ano.isEmpty()) {
            editTextLancamento.setError("Informe o ano de lançamento");
            editTextLancamento.requestFocus();
            return;
        }
        if (notaTexto.isEmpty()) {
            editTextNota.setError("Informe a nota pessoal");
            editTextNota.requestFocus();
            return;
        }

        double nota;
        try {
            nota = Double.parseDouble(notaTexto);
        } catch (NumberFormatException e) {
            editTextNota.setError("Nota inválida");
            editTextNota.requestFocus();
            return;
        }
        if (nota < 0 || nota > 10) {
            editTextNota.setError("A nota deve ser entre 0 e 10");
            editTextNota.requestFocus();
            return;
        }

        String tipo = spinnerTipo.getSelectedItem().toString();
        String genero = spinnerGenero.getSelectedItem().toString();

        if (filmeEditando == null) {
            Filme filme = new Filme(null, nome, ano, genero, tipo, assistido, nota);
            db.collection("filmes")
                    .add(filme)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Filme salvo!", Toast.LENGTH_SHORT).show();

                        carregarFilmes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
        } else {
            filmeEditando.setNome(nome);
            filmeEditando.setAnoLancamento(ano);
            filmeEditando.setTipo(tipo);
            filmeEditando.setGenero(genero);
            filmeEditando.setAssistido(assistido);
            filmeEditando.setNota(nota);

            db.collection("filmes").document(filmeEditando.getId())
                    .set(filmeEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Filme atualizado!", Toast.LENGTH_SHORT).show();

                        carregarFilmes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show());
        }
    }

    private void confirmarExclusao(Filme filme) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Deseja excluir \"" + filme.getNome() + "\"?")
                .setPositiveButton("Excluir", (d, w) -> deletarFilme(filme))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarFilme(Filme filme) {
        if (filme.getId() == null) return;
        db.collection("filmes").document(filme.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (filmeEditando != null && filme.getId().equals(filmeEditando.getId())) {

                    }
                    Toast.makeText(this, "Filme excluído!", Toast.LENGTH_SHORT).show();
                    carregarFilmes();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show());
    }


    private void carregarFilmes() {
        db.collection("filmes")
                .get()
                .addOnSuccessListener(query -> {
                    listaCompleta.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Filme f = doc.toObject(Filme.class);
                        f.setId(doc.getId());
                        listaCompleta.add(f);
                    }
                    aplicarFiltro();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar filmes", Toast.LENGTH_SHORT).show());
    }


    private void aplicarFiltro() {
        String busca = editTextBusca.getText().toString().trim().toLowerCase();
        listaFilme.clear();
        for (Filme f : listaCompleta) {
            if (busca.isEmpty()
                    || (f.getNome() != null && f.getNome().toLowerCase().contains(busca))) {
                listaFilme.add(f);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void selecionarSpinner(Spinner spinner, String valor) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> a = (ArrayAdapter<String>) spinner.getAdapter();
        int pos = valor == null ? -1 : a.getPosition(valor);
        spinner.setSelection(pos >= 0 ? pos : 0);
    }
}