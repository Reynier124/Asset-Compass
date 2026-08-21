import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getBrokers } from 'app/entities/broker/broker.reducer';

import { createEntity, getEntity, reset, updateEntity } from './broker-account.reducer';

export const BrokerAccountUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokers = useAppSelector(state => state.gateway.broker.entities);
  const brokerAccountEntity = useAppSelector(state => state.gateway.brokerAccount.entity);
  const loading = useAppSelector(state => state.gateway.brokerAccount.loading);
  const updating = useAppSelector(state => state.gateway.brokerAccount.updating);
  const updateSuccess = useAppSelector(state => state.gateway.brokerAccount.updateSuccess);

  const handleClose = () => {
    navigate('/broker-account');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokers({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...brokerAccountEntity,
      ...values,
      broker: brokers.find(it => it.id.toString() === values.broker?.toString()),
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
          ...brokerAccountEntity,
          broker: brokerAccountEntity?.broker?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.brokerAccount.home.createOrEditLabel" data-cy="BrokerAccountCreateUpdateHeading">
            <Translate contentKey="gatewayApp.brokerAccount.home.createOrEditLabel">Create or edit a BrokerAccount</Translate>
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
                  id="broker-account-id"
                  label={translate('gatewayApp.brokerAccount.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('gatewayApp.brokerAccount.externalAccountId')}
                id="broker-account-externalAccountId"
                name="externalAccountId"
                data-cy="externalAccountId"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.brokerAccount.displayName')}
                id="broker-account-displayName"
                name="displayName"
                data-cy="displayName"
                type="text"
              />
              <ValidatedField
                id="broker-account-broker"
                name="broker"
                data-cy="broker"
                label={translate('gatewayApp.brokerAccount.broker')}
                type="select"
                required
              >
                <option value="" key="0" />
                {brokers
                  ? brokers.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/broker-account" replace variant="info">
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

export default BrokerAccountUpdate;
