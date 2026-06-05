import type { ContagemItem } from '../stats';

interface BarListProps {
  itens: ContagemItem[];
  /** Classe CSS opcional por item (ex.: cor por status). */
  classePorItem?: (item: ContagemItem) => string;
}

/** Lista de barras horizontais proporcionais (mini-gráfico). */
export default function BarList({ itens, classePorItem }: BarListProps) {
  if (itens.length === 0) {
    return <p className="comentario-vazio">Sem dados para exibir.</p>;
  }
  const maximo = Math.max(...itens.map((i) => i.quantidade), 1);

  return (
    <div className="barlist">
      {itens.map((item) => (
        <div key={item.chave} className="barlist-row">
          <span className="barlist-label">{item.label}</span>
          <div className="barlist-track">
            <div
              className={`barlist-fill ${classePorItem?.(item) ?? ''}`}
              style={{ width: `${(item.quantidade / maximo) * 100}%` }}
            />
          </div>
          <span className="barlist-valor">{item.quantidade}</span>
        </div>
      ))}
    </div>
  );
}
