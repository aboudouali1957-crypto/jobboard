import { FormControl, FormGroup } from '@angular/forms';
import { validateCvFile } from '../features/candidate/candidate-profile';
import { formatSalary, timeAgo } from './pipes';
import { passwordsMatchValidator, salaryRangeValidator } from './validators';

describe('formatSalary', () => {
  // Intl insère des espaces insécables : on les normalise pour comparer
  const normalize = (value: string) => value.replace(/\s/g, ' ');

  it('affiche une fourchette, un minimum ou un maximum', () => {
    expect(normalize(formatSalary(45000, 55000))).toBe('45 000 € – 55 000 €');
    expect(normalize(formatSalary(45000, null))).toBe('À partir de 45 000 €');
    expect(normalize(formatSalary(null, 60000))).toBe("Jusqu'à 60 000 €");
    expect(formatSalary(null, null)).toBe('Salaire non communiqué');
  });
});

describe('timeAgo', () => {
  const now = new Date('2026-10-11T12:00:00Z');

  it('exprime une date en durée relative', () => {
    expect(timeAgo('2026-10-11T08:00:00Z', now)).toBe("aujourd'hui");
    expect(timeAgo('2026-10-10T08:00:00Z', now)).toBe('hier');
    expect(timeAgo('2026-10-01T12:00:00Z', now)).toBe('il y a 10 jours');
    expect(timeAgo('2026-07-01T12:00:00Z', now)).toBe('il y a 3 mois');
  });
});

describe('validateurs', () => {
  function salaryForm(min: number | null, max: number | null) {
    return new FormGroup(
      { salaryMin: new FormControl(min), salaryMax: new FormControl(max) },
      { validators: salaryRangeValidator },
    );
  }

  it('refuse un salaire maximum inférieur au minimum', () => {
    expect(salaryForm(50000, 40000).hasError('salaryRange')).toBe(true);
    expect(salaryForm(40000, 50000).valid).toBe(true);
    expect(salaryForm(null, 40000).valid).toBe(true);
  });

  it('vérifie la confirmation du mot de passe', () => {
    const form = new FormGroup(
      { password: new FormControl('Secret123'), confirmPassword: new FormControl('Secret124') },
      { validators: passwordsMatchValidator },
    );
    expect(form.hasError('passwordsMismatch')).toBe(true);

    form.controls.confirmPassword.setValue('Secret123');
    expect(form.valid).toBe(true);
  });
});

describe('validateCvFile', () => {
  it('accepte un PDF de moins de 5 Mo', () => {
    expect(validateCvFile(new File(['%PDF-1.4'], 'cv.pdf', { type: 'application/pdf' }))).toBeNull();
  });

  it('refuse les autres formats et les fichiers trop lourds', () => {
    expect(validateCvFile(new File(['x'], 'cv.docx', { type: 'application/msword' }))).toContain('PDF');

    const big = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'cv.pdf', { type: 'application/pdf' });
    expect(validateCvFile(big)).toContain('5 Mo');
  });
});
