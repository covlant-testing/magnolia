class CategoryDataMapper {

  toCategoryViewModel(serviceType, response) {
    return serviceType == 'graphQL' ? this.fromGraphQL(response) : this.fromRest(response);
  }

  fromGraphQL(response) {
    const categories = response.data.data.tourCategories
    return categories.map(category => {
      return {
        id: category._metadata.id,
        displayName: category.displayName
      }
    });
  }

  fromRest(response) {
    const categories = response.data.results;
    return categories.map(category => {
      return {
        id: category['@id'],
        displayName: category.displayName
      }
    });
  }
}