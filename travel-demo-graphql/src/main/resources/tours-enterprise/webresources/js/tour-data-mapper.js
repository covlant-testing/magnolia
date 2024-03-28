class TourDataMapper {

  baseContext;

  constructor(baseContext) {
    this.baseContext = baseContext;
  }

  mapToTourViews(serviceType, response) {
    return this.mapTourData(serviceType, response);
  }

  mapTourData(serviceType, response) {
    return serviceType === 'graphQL' ? this.fromGraphQL(response) : this.fromRest(response);
  }

  fromRest(response) {
    let tours = response.data.results;
    return tours.map(tour => new RestDataMapper().convert(tour, this.baseContext));
  }

  fromGraphQL(response) {
    let tours = response.data.data.tours;
    return tours.map(tour => new GraphQLDataMapper().convert(tour));
  }
}

class GraphQLDataMapper {
  convert(tourResponse) {
    return {
      path: tourResponse._metadata.path,
      name: tourResponse.name,
      duration: tourResponse.duration,
      image: { renditions: this.buildImage(tourResponse) },
      tourTypes: this.buildTourType(tourResponse),
      destination: tourResponse.destination
    };
  }

  buildImage(tourResponse) {
    return tourResponse.image.renditions.reduce(function(map, obj) {
      map[obj.renditionName] = obj;
      return map;
    }, {});
  }

  buildTourType(tourResponse) {
    return tourResponse.tourTypes.map(tourType => {
      return {
        icon: { link: tourType.icon.link },
        displayName: tourType.displayName
      }
    })
  }
}

class RestDataMapper {
  convert(tourResponse, baseContext) {
    return {
      path: tourResponse['@path'],
      name: tourResponse.name,
      duration: tourResponse.duration,
      image: tourResponse.image,
      tourTypes: this.buildTourType(tourResponse, baseContext),
      destination: tourResponse.destination
    };
  }

  buildTourType(tourResponse, baseContext) {
    return tourResponse.tourTypes.map(tourType => {
      return {
        icon: { link: `${baseContext}/dam/${tourType.icon}` },
        displayName: tourType.displayName
      };
    })
  }
}