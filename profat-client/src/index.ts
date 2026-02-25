/**
 * Profat JavaScript client – analytics and telemetry API.
 *
 * Usage (ESM):
 *   import { createClient } from 'profat-client';
 *   const client = createClient({ baseUrl: 'http://localhost:9002/api/v1' });
 *   await client.logPageVisit(serviceId, { pageUrl: '/', isMobile: false });
 *
 * Usage (script tag): include profat.iife.js then use window.Profat.createClient(...)
 */

export { ProfatClient, ProfatError, createClient } from './client.js';
export type {
  AnalyticsSummary,
  AnalyticsSummaryOptions,
  EventCreateRequest,
  EventListItem,
  EventResponse,
  ListEventsOptions,
  Page,
  PageVisitPayload,
  ProfatConfig,
  ServiceCreateRequest,
  ServiceResponse,
} from './types.js';
