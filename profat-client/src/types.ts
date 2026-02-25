/**
 * Profat API types (matches the REST API).
 */

export interface ProfatConfig {
  /** Base URL of the Profat API (e.g. `http://localhost:9002/api/v1`) */
  baseUrl: string;
}

/** Registered service (app) returned by the API */
export interface ServiceResponse {
  id: string;
  name: string;
  createdAt: string;
}

/** Request body to create a service */
export interface ServiceCreateRequest {
  name: string;
}

/** Request body to log an event (action + payload) */
export interface EventCreateRequest {
  action: string;
  payload?: Record<string, unknown>;
}

/** Response after logging an event */
export interface EventResponse {
  id: string;
  action: string;
  createdAt: string;
}

/** Event item in a paginated list */
export interface EventListItem {
  id: string;
  action: string;
  createdAt: string;
  payload: string | null;
}

/** Spring Data Page wrapper */
export interface Page<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Options for listing events */
export interface ListEventsOptions {
  page?: number;
  size?: number;
  search?: string;
}

/** Options for analytics summary */
export interface AnalyticsSummaryOptions {
  from?: string;
  to?: string;
}

/** Analytics summary response */
export interface AnalyticsSummary {
  visitsPerDay: Array<{ date: string; count: number }>;
  uniqueCountries: string[];
  totalPageLoads: number;
  pageLoadsByDevice: { mobile: number; desktop: number };
}

/** Predefined payload shape for the `page_visit` action */
export interface PageVisitPayload {
  pageUrl?: string;
  country?: string;
  isMobile?: boolean;
  userAgent?: string;
  [key: string]: unknown;
}
