import type {
  AnalyticsSummary,
  AnalyticsSummaryOptions,
  EventCreateRequest,
  EventListItem,
  EventResponse,
  ListEventsOptions,
  Page,
  ProfatConfig,
  ServiceCreateRequest,
  ServiceResponse,
} from './types.js';

export class ProfatClient {
  private readonly baseUrl: string;
  private readonly apiKey: string | undefined;

  constructor(config: ProfatConfig) {
    this.baseUrl = config.baseUrl.replace(/\/$/, '');
    this.apiKey = config.apiKey;
  }

  private async request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.baseUrl}${path}`;
    const res = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(this.apiKey !== undefined && this.apiKey !== ''
          ? { 'X-Profat-Key': this.apiKey }
          : {}),
        ...options.headers,
      },
    });
    if (!res.ok) {
      const text = await res.text();
      let body: unknown = text;
      try {
        body = JSON.parse(text);
      } catch {
        // leave as text
      }
      throw new ProfatError(res.status, res.statusText, body);
    }
    if (res.status === 204 || res.headers.get('content-length') === '0') {
      return undefined as T;
    }
    return res.json() as Promise<T>;
  }

  /** Create a new service (register an app). */
  async createService(request: ServiceCreateRequest): Promise<ServiceResponse> {
    return this.request<ServiceResponse>('/services', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  /** List all registered services. */
  async listServices(): Promise<ServiceResponse[]> {
    return this.request<ServiceResponse[]>('/services');
  }

  /** Log an event (any action, including `page_visit`). */
  async logEvent(serviceId: string, event: EventCreateRequest): Promise<EventResponse> {
    return this.request<EventResponse>(`/services/${serviceId}/events`, {
      method: 'POST',
      body: JSON.stringify(event),
    });
  }

  /** Log a page visit (convenience for action `page_visit`). */
  async logPageVisit(
    serviceId: string,
    payload: {
      pageUrl?: string;
      country?: string;
      isMobile?: boolean;
      userAgent?: string;
      [key: string]: unknown;
    }
  ): Promise<EventResponse> {
    return this.logEvent(serviceId, {
      action: 'page_visit',
      payload,
    });
  }

  /** List events for a service (paginated, optional search). */
  async listEvents(
    serviceId: string,
    options: ListEventsOptions = {}
  ): Promise<Page<EventListItem>> {
    const params = new URLSearchParams();
    if (options.page != null) params.set('page', String(options.page));
    if (options.size != null) params.set('size', String(options.size));
    if (options.search != null && options.search !== '')
      params.set('search', options.search);
    const qs = params.toString();
    return this.request<Page<EventListItem>>(
      `/services/${serviceId}/events${qs ? `?${qs}` : ''}`
    );
  }

  /** List distinct action names for a service. */
  async listActions(serviceId: string): Promise<string[]> {
    return this.request<string[]>(`/services/${serviceId}/actions`);
  }

  /** Get analytics summary for a service (optional date range). */
  async getAnalyticsSummary(
    serviceId: string,
    options: AnalyticsSummaryOptions = {}
  ): Promise<AnalyticsSummary> {
    const params = new URLSearchParams();
    if (options.from != null) params.set('from', options.from);
    if (options.to != null) params.set('to', options.to);
    const qs = params.toString();
    return this.request<AnalyticsSummary>(
      `/services/${serviceId}/analytics/summary${qs ? `?${qs}` : ''}`
    );
  }
}

/** Error thrown when the API returns a non-2xx response */
export class ProfatError extends Error {
  constructor(
    public readonly status: number,
    public readonly statusText: string,
    public readonly body: unknown
  ) {
    super(`Profat API error ${status}: ${statusText}`);
    this.name = 'ProfatError';
  }
}

/**
 * Create a Profat API client.
 * @param config - baseUrl (e.g. `http://localhost:9002/api/v1`)
 */
export function createClient(config: ProfatConfig): ProfatClient {
  return new ProfatClient(config);
}
