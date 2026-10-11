import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { API } from '../http-utils';
import { Company, CompanyRequest, Skill } from '../models';

/** Données de référence (compétences) et entreprise du recruteur. */
@Injectable({ providedIn: 'root' })
export class ReferenceApi {
  private readonly http = inject(HttpClient);

  /** Le référentiel change rarement : une seule requête par session. */
  private readonly skills$ = this.http.get<Skill[]>(`${API}/skills`).pipe(shareReplay({ bufferSize: 1, refCount: false }));

  skills(): Observable<Skill[]> {
    return this.skills$;
  }

  myCompany(): Observable<Company> {
    return this.http.get<Company>(`${API}/recruiter/company`);
  }

  updateMyCompany(request: CompanyRequest): Observable<Company> {
    return this.http.put<Company>(`${API}/recruiter/company`, request);
  }
}
