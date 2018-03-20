-- Delete status when layer is removed with cascade. Otherwise prevents monitored layers from being removed.
alter table oskari_backendstatus drop constraint oskari_backendstatus_maplayer_id_fkey;
alter table oskari_backendstatus add CONSTRAINT oskari_backendstatus_maplayer_id_fkey FOREIGN KEY (maplayer_id)
      REFERENCES oskari_maplayer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;