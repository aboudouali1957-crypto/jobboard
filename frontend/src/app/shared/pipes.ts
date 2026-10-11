import { Pipe, PipeTransform } from '@angular/core';
import { CONTRACT_LABELS, STATUS_LABELS } from '../core/labels';
import { ApplicationStatus, ContractType } from '../core/models';

const euros = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 });

/** Formate une fourchette de salaire annuel brut : « 45 000 € – 55 000 € ». */
export function formatSalary(min: number | null, max: number | null): string {
  if (min !== null && max !== null) {
    return min === max ? euros.format(min) : `${euros.format(min)} – ${euros.format(max)}`;
  }
  if (min !== null) {
    return `À partir de ${euros.format(min)}`;
  }
  if (max !== null) {
    return `Jusqu'à ${euros.format(max)}`;
  }
  return 'Salaire non communiqué';
}

@Pipe({ name: 'salary' })
export class SalaryPipe implements PipeTransform {
  transform(value: { salaryMin: number | null; salaryMax: number | null }): string {
    return formatSalary(value.salaryMin, value.salaryMax);
  }
}

@Pipe({ name: 'contractLabel' })
export class ContractLabelPipe implements PipeTransform {
  transform(value: ContractType): string {
    return CONTRACT_LABELS[value] ?? value;
  }
}

@Pipe({ name: 'statusLabel' })
export class StatusLabelPipe implements PipeTransform {
  transform(value: ApplicationStatus): string {
    return STATUS_LABELS[value] ?? value;
  }
}

/** Date relative courte : « aujourd'hui », « il y a 3 jours », « il y a 2 mois ». */
export function timeAgo(iso: string, now: Date = new Date()): string {
  const days = Math.floor((now.getTime() - new Date(iso).getTime()) / 86_400_000);
  if (days <= 0) {
    return "aujourd'hui";
  }
  if (days === 1) {
    return 'hier';
  }
  if (days < 30) {
    return `il y a ${days} jours`;
  }
  const months = Math.floor(days / 30);
  return months < 12 ? `il y a ${months} mois` : `il y a ${Math.floor(months / 12)} an(s)`;
}

@Pipe({ name: 'timeAgo' })
export class TimeAgoPipe implements PipeTransform {
  transform(value: string): string {
    return timeAgo(value);
  }
}
