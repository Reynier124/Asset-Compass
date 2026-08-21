import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('AssetRatio e2e test', () => {
  const assetRatioPageUrl = '/asset-ratio';
  let username: string;
  let password: string;
  const assetRatioSample = { ratio: 'beside', effectiveFrom: '2023-12-22' };

  let assetRatio;
  let asset;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/assets',
      body: { ticket: 'anti request potentially', category: 'officially', country: 'Uzbekistan', description: 'phooey yellow seagull' },
    }).then(({ body }) => {
      asset = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/asset-ratios+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/asset-ratios').as('postEntityRequest');
    cy.intercept('DELETE', '/api/asset-ratios/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/assets', {
      statusCode: 200,
      body: [asset],
    });
  });

  afterEach(() => {
    if (assetRatio) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/asset-ratios/${assetRatio.id}`,
      }).then(() => {
        assetRatio = undefined;
      });
    }
  });

  afterEach(() => {
    if (asset) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/assets/${asset.id}`,
      }).then(() => {
        asset = undefined;
      });
    }
  });

  it('AssetRatios menu should load AssetRatios page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('asset-ratio');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('AssetRatio').should('exist');
    cy.location('pathname').should('eq', assetRatioPageUrl);
  });

  describe('AssetRatio page', () => {
    it('should have translated page title', () => {
      cy.visit(assetRatioPageUrl);
      cy.getEntityHeading('AssetRatio').should('not.contain', 'gatewayApp.assetRatio.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(assetRatioPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create AssetRatio page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${assetRatioPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('AssetRatio');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', assetRatioPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/asset-ratios',
          body: {
            ...assetRatioSample,
            asset,
          },
        }).then(({ body }) => {
          assetRatio = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/asset-ratios+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [assetRatio],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(assetRatioPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details AssetRatio page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('assetRatio');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', assetRatioPageUrl);
      });

      it('edit button click should load edit AssetRatio page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('AssetRatio');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', assetRatioPageUrl);
      });

      it('edit button click should load edit AssetRatio page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('AssetRatio');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', assetRatioPageUrl);
      });

      it('last delete button click should delete instance of AssetRatio', () => {
        cy.intercept('GET', '/api/asset-ratios/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('assetRatio').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', assetRatioPageUrl);

        assetRatio = undefined;
      });
    });
  });

  describe('new AssetRatio page', () => {
    beforeEach(() => {
      cy.visit(assetRatioPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('AssetRatio');
    });

    it('should create an instance of AssetRatio', () => {
      cy.get(`[data-cy="ratio"]`).type('waft');
      cy.get(`[data-cy="ratio"]`).should('have.value', 'waft');

      cy.get(`[data-cy="effectiveFrom"]`).type('2023-12-22');
      cy.get(`[data-cy="effectiveFrom"]`).blur();
      cy.get(`[data-cy="effectiveFrom"]`).should('have.value', '2023-12-22');

      cy.get(`[data-cy="asset"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        assetRatio = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', assetRatioPageUrl);
    });
  });
});
