-- V1__alter_target_weight_column.sql

ALTER TABLE client
ALTER COLUMN target_weight TYPE FLOAT USING target_weight::FLOAT;
