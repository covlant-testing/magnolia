const NO_WRAPPED_VALUE_FIELD = ['@duration'];
class GraphqlTourQueriesProvider {
  constructor(httpClient, baseContext) {
    this.httpClient = httpClient;
    this.baseContext = baseContext;
  }

  getTourQueries() {
   return this.httpClient.get(`${this.baseContext}/.resources/tours-enterprise/webresources/queries/Tours.graphql`);
  }

  getCategoryQueries() {
    return this.httpClient.get(`${this.baseContext}/.resources/tours-enterprise/webresources/queries/TourCategory.graphql`);
  }

  buildGraphQLFilterQueryParams(parameters) {
    const durationQuery = this.buildDurationQuery(parameters.duration);
    const tourTypesQuery = this.buildTourTypeQuery(parameters.tourTypes);
    const destinationQuery = this.buildDestinationQuery(parameters.destination);
    const ftsQuery = this.buildFTSQuery(parameters.q);
    return [durationQuery, tourTypesQuery, destinationQuery, ftsQuery]
      .filter(item => item.length > 0)
      .join(' AND ')
  }

  buildDurationQuery(durations) {
    const NAMED_QUERY = '@duration';
    return this.buildFilterQuery(durations, NAMED_QUERY);
  }

  buildTourTypeQuery(tourTypes) {
    const NAMED_QUERY = '@tourTypes';
    return this.buildFilterQuery(tourTypes, NAMED_QUERY);
  }

  buildDestinationQuery(destinations) {
    const NAMED_QUERY = '@destination';
    return this.buildFilterQuery(destinations, NAMED_QUERY);
  }

  buildFilterQuery(values, namedQuery) {
    if (!values || !values.length) {
      return '';
    }
    return values.map(value => `${namedQuery}=${this.queryValueWrapper(value, namedQuery)}`)
      .join(' AND ')
  }

  queryValueWrapper(value, namedQuery) {
    if (NO_WRAPPED_VALUE_FIELD.includes(namedQuery)) {
      return /^\d+$/.test(value) ? value : '0';
    }
    return `'${this.sanitizeQueryValue(value)}'`;
  }

  sanitizeQueryValue(value) {
    return String(value)
      .replace(/'/g, "''")
      .replace(/%/g, '\\%')
      .replace(/_/g, '\\_');
  }

  buildFTSQuery(queryValue) {
    if (!queryValue || !queryValue.length) {
      return '';
    }

    const sanitized = this.sanitizeQueryValue(queryValue);
    const query = ['@name', '@description', '@body', '@location'].map(field => {
      return `${field} LIKE '%${sanitized}%'`;
    }).join(' OR ');

    return `(${query})`;
  }
}