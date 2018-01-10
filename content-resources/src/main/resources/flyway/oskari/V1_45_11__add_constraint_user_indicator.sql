ALTER TABLE oskari_user_indicator ADD PRIMARY KEY (id);

ALTER TABLE oskari_user_indicator ADD CONSTRAINT oskari_user_indicator_user_fk
 FOREIGN KEY (user_id) REFERENCES oskari_users (id) SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
