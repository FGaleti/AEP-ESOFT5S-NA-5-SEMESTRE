import type { Status } from '../types';
import { STATUS_LABEL, STATUS_CLASSE } from '../constants';

interface StatusBadgeProps {
  status: Status;
  label?: string;
}

export default function StatusBadge({ status, label }: StatusBadgeProps) {
  return (
    <span className={`badge ${STATUS_CLASSE[status]}`}>
      {label ?? STATUS_LABEL[status]}
    </span>
  );
}
