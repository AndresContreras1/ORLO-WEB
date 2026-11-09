import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';

import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { Metrics } from '../../models/metrics.model';
import { MetricsService } from '../../services/metrics.service';
import { errorMessage } from '../../utils/error-message';

interface MetricCard {
  label: string;
  value: string;
  hint: string;
  icon: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [LoadStateComponent],
  providers: [DecimalPipe, CurrencyPipe],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private metricsService = inject(MetricsService);
  private decimal = inject(DecimalPipe);
  private currency = inject(CurrencyPipe);

  cargando = signal(true);
  error = signal<string | null>(null);
  tarjetas = signal<MetricCard[]>([]);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.metricsService.current().subscribe({
      next: (m) => {
        this.tarjetas.set(this.aTarjetas(m));
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  private aTarjetas(m: Metrics): MetricCard[] {
    const n = (v: number) => this.decimal.transform(v, '1.0-0') ?? '0';
    return [
      { label: 'Registros', value: n(m.totalUsers), hint: 'Usuarios que abrieron la app', icon: 'bi-person-plus' },
      { label: 'Premium', value: n(m.premiumUsers), hint: `${m.conversionRate}% de conversión`, icon: 'bi-stars' },
      { label: 'Cuestionarios', value: n(m.quizzesCompleted), hint: 'Diagnósticos completados', icon: 'bi-ui-checks' },
      { label: 'Mensajes al asesor', value: n(m.advisorMessages), hint: `${n(m.advisorMessagesToday)} hoy`, icon: 'bi-chat-heart' },
      {
        label: 'Costo de la IA',
        value: this.currency.transform(m.aiCostUsd, 'USD', 'symbol', '1.2-4') ?? '$0',
        hint: 'Acumulado',
        icon: 'bi-cash-coin',
      },
      { label: 'Contenido', value: n(m.publishedLessons), hint: `lecciones · ${n(m.activeStores)} tiendas activas`, icon: 'bi-journal-text' },
    ];
  }
}
