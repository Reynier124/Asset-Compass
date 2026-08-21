import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getAssets } from 'app/entities/asset/asset.reducer';

import { createEntity, getEntity, reset, updateEntity } from './asset-ratio.reducer';

export const AssetRatioUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const assets = useAppSelector(state => state.gateway.asset.entities);
  const assetRatioEntity = useAppSelector(state => state.gateway.assetRatio.entity);
  const loading = useAppSelector(state => state.gateway.assetRatio.loading);
  const updating = useAppSelector(state => state.gateway.assetRatio.updating);
  const updateSuccess = useAppSelector(state => state.gateway.assetRatio.updateSuccess);

  const handleClose = () => {
    navigate('/asset-ratio');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getAssets({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...assetRatioEntity,
      ...values,
      asset: assets.find(it => it.id.toString() === values.asset?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...assetRatioEntity,
          asset: assetRatioEntity?.asset?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.assetRatio.home.createOrEditLabel" data-cy="AssetRatioCreateUpdateHeading">
            <Translate contentKey="gatewayApp.assetRatio.home.createOrEditLabel">Create or edit a AssetRatio</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="asset-ratio-id"
                  label={translate('gatewayApp.assetRatio.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('gatewayApp.assetRatio.ratio')}
                id="asset-ratio-ratio"
                name="ratio"
                data-cy="ratio"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.assetRatio.effectiveFrom')}
                id="asset-ratio-effectiveFrom"
                name="effectiveFrom"
                data-cy="effectiveFrom"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="asset-ratio-asset"
                name="asset"
                data-cy="asset"
                label={translate('gatewayApp.assetRatio.asset')}
                type="select"
                required
              >
                <option value="" key="0" />
                {assets
                  ? assets.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/asset-ratio" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default AssetRatioUpdate;
