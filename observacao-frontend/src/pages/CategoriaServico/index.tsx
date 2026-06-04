export default function CategoriaServico() {
  return (
    <div className="page-container">
      <h2>Categoria de Serviço</h2>
      <p>Selecione uma categoria para registrar uma solicitação</p>
      <div style={{ marginTop: '20px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
        <button>💡 Iluminação</button>
        <button>🕳️ Buraco</button>
        <button>🧹 Limpeza</button>
        <button>🏥 Saúde</button>
        <button>🏫 Segurança Escolar</button>
        <button>🌳 Poda de Árvore</button>
        <button>🚰 Saneamento</button>
      </div>
    </div>
  );
}
