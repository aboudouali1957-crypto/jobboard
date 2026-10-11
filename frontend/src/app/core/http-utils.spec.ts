import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';
import { applyServerErrors, errorMessage, fileNameFrom, toParams } from './http-utils';

describe('http-utils', () => {
  it('toParams ignore les valeurs vides et ajoute la pagination', () => {
    const params = toParams(
      { keyword: 'java', city: '', contractType: null, remote: false, minSalary: 40000 },
      { page: 2, size: 20, sort: 'createdAt,desc' },
    );

    expect(params.keys().sort()).toEqual(['keyword', 'minSalary', 'page', 'size', 'sort']);
    expect(params.get('minSalary')).toBe('40000');
    expect(params.get('page')).toBe('2');
  });

  it('errorMessage lit le champ detail du ProblemDetail', () => {
    const error = new HttpErrorResponse({ status: 409, error: { detail: 'Vous avez déjà postulé à cette offre' } });

    expect(errorMessage(error)).toBe('Vous avez déjà postulé à cette offre');
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Serveur injoignable');
    expect(errorMessage(new Error('boom'), 'Par défaut')).toBe('Par défaut');
  });

  it('applyServerErrors reporte les erreurs de validation sur les champs', () => {
    const form = new FormGroup({ title: new FormControl(''), salaryMax: new FormControl<number | null>(null) });
    const error = new HttpErrorResponse({
      status: 400,
      error: { errors: { title: 'ne doit pas être vide', salaryRangeValid: 'incohérent' } },
    });

    const applied = applyServerErrors(form, error, { salaryRangeValid: 'salaryMax' });

    expect(applied).toBe(true);
    expect(form.controls.title.getError('server')).toBe('ne doit pas être vide');
    expect(form.controls.salaryMax.getError('server')).toBe('incohérent');
  });

  it('applyServerErrors ignore les erreurs qui ne sont pas des 400', () => {
    const form = new FormGroup({ title: new FormControl('') });

    expect(applyServerErrors(form, new HttpErrorResponse({ status: 409, error: {} }))).toBe(false);
  });

  it('fileNameFrom gère les noms encodés et simples', () => {
    expect(fileNameFrom("inline; filename*=UTF-8''CV-In%C3%A8s.pdf")).toBe('CV-Inès.pdf');
    expect(fileNameFrom('attachment; filename="cv.pdf"')).toBe('cv.pdf');
    expect(fileNameFrom(null)).toBeNull();
  });
});
