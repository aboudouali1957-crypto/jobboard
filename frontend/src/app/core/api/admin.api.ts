import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API, toParams } from '../http-utils';
import { AdminStats, AdminUser, Page, PageRequest, Role, Skill } from '../models';

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private readonly http = inject(HttpClient);

  stats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${API}/admin/stats`);
  }

  users(filters: { role: Role | null; search: string | null }, page: PageRequest): Observable<Page<AdminUser>> {
    return this.http.get<Page<AdminUser>>(`${API}/admin/users`, { params: toParams(filters, page) });
  }

  setEnabled(id: number, enabled: boolean): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${API}/admin/users/${id}/enabled`, { enabled });
  }

  createSkill(name: string): Observable<Skill> {
    return this.http.post<Skill>(`${API}/admin/skills`, { name });
  }

  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/admin/offers/${id}`);
  }
}
