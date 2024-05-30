package org.oskari.init;

/**
 * Interface to implement customization for overriding the built-in migration process
 */
public interface CustomMigration {
    void migrateDB();
}
