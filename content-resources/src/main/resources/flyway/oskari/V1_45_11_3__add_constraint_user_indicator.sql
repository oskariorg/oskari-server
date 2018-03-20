-- setup constraint for id
ALTER TABLE oskari_user_indicator ADD PRIMARY KEY (id);

-- add user constraint
ALTER TABLE oskari_user_indicator ADD CONSTRAINT oskari_user_indicator_user_fk
 FOREIGN KEY (user_id) REFERENCES oskari_users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
