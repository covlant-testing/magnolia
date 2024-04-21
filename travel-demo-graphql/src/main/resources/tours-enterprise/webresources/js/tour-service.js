class TourService {
  serviceImp;
  location

  constructor(contextPath, httpService, location, serviceType) {
    this.serviceImp = this.initService(serviceType, contextPath, httpService);
    this.location = location;
  }

  initService(serviceType, contextPath, httpService ) {
    return serviceType === 'graphQL' ? new GraphQLService(contextPath, httpService) : new RestService(contextPath, httpService);
  }

  getDestinations(language) {
    return this.serviceImp.getDestinations(language);
  }

  getTourTypes(language) {
    return this.serviceImp.getTourTypes(language);
  }

  filterTours(parameters) {
    this.updateLocation(parameters);
    return this.serviceImp.filterTours(parameters)
  }

  updateLocation(parameters) {
    Object.keys(parameters).forEach(key => {
      if (parameters[key].length > 0) {
        let values = parameters[key];
        this.location.search(key, values.join(','));
      } else {
        this.location.search(key, null);
      }
    });
  }
}

class RestService {
  restBase = '/.rest/delivery';
  contextPath;
  httpService;

  constructor(contextPath, httpService) {
    this.contextPath = contextPath;
    this.httpService = httpService;
  }

  getDestinations(language) {
    return this.getCategory(language, 'destinations');
  }

  getTourTypes(language) {
    return this.getCategory(language, 'tourTypes');
  }

  getCategory(language, categoryType) {
    const relativePath = this.getCategoryRequestPath(language, categoryType);
    return this.httpService.get(relativePath)
      .then(response => new CategoryDataMapper().toCategoryViewModel('rest', response));
  }

  getCategoryRequestPath(language, categoryType) {
    return `${this.contextPath}${this.restBase}/${categoryType}/v1/?lang=${language}`;
  }

  filterTours(parameters) {
    return this.httpService.get(`${this.contextPath}${this.restBase }/tours/v1/${this.buildRestQuery(parameters)}`)
      .then(response => new TourDataMapper(this.contextPath).mapToTourViews('rest', response));
  }

  buildRestQuery(parameters) {
    let query = Object.keys(parameters).map(key => {
      if (parameters[key].length == 0) {
        return '';
      }
      let values = parameters[key];
      return `${key}=${values.join(',')}`;
    }).filter(val => val.length!== 0)
      .join('&');

    return `?${query}`;
  }
}

class GraphQLService {
  graphQLBase ='/.graphql';
  headers = { 'Content-type': 'application/json' };
  contextPath;
  httpService;
  queryProvider;

  constructor(contextPath, httpService) {
    this.contextPath = contextPath;
    this.httpService = httpService;
    this.queryProvider = new GraphqlTourQueriesProvider(httpService, contextPath);
  }

  getDestinations(language) {
    return this.getCategory('/destinations');
  }

  getTourTypes(language){
    return this.getCategory('/tour-types');
  }

  getCategory(queryPath) {
    return this.queryProvider.getCategoryQueries()
      .then(query => this.getCategories(query, queryPath)
        .then(response => new CategoryDataMapper().toCategoryViewModel('graphQL', response)));
  }

  getCategories(query, queryPath) {
    return this.httpService
      .post(this.contextPath + this.graphQLBase,
        { query: query.data, variables: { path: queryPath } },
        { headers: this.headers }
      );
  }

  filterTours(parameters) {
    return this.queryProvider.getTourQueries().then(query => {
      const filter = new GraphqlTourQueriesProvider().buildGraphQLFilterQueryParams(parameters);
      return this.httpService.post(this.contextPath + this.graphQLBase,
        { query: query.data, variables: { filter: filter } },
        { headers: this.headers })
    }).then(response => new TourDataMapper(this.contextPath).mapToTourViews('graphQL', response));
  }
}