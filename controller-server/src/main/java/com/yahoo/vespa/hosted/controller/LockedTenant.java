// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.yahoo.config.provision.TenantName;
import com.yahoo.security.KeyUtils;
import com.yahoo.transaction.Mutex;
import com.yahoo.vespa.athenz.api.AthenzDomain;
import com.yahoo.vespa.hosted.controller.api.identifiers.Property;
import com.yahoo.vespa.hosted.controller.api.identifiers.PropertyId;
import com.yahoo.vespa.hosted.controller.api.integration.organization.Contact;
import com.yahoo.vespa.hosted.controller.api.integration.secrets.TenantSecretStore;
import com.yahoo.vespa.hosted.controller.api.role.SimplePrincipal;
import com.yahoo.vespa.hosted.controller.tenant.ArchiveAccess;
import com.yahoo.vespa.hosted.controller.tenant.AthenzTenant;
import com.yahoo.vespa.hosted.controller.tenant.CloudTenant;
import com.yahoo.vespa.hosted.controller.tenant.DeletedTenant;
import com.yahoo.vespa.hosted.controller.tenant.LastLoginInfo;
import com.yahoo.vespa.hosted.controller.tenant.Tenant;
import com.yahoo.vespa.hosted.controller.tenant.TenantInfo;

import java.security.Principal;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A tenant that has been locked for modification. Provides methods for modifying a tenant's fields.
 *
 * @author mpolden
 * @author jonmv
 */
public abstract class LockedTenant {

    final TenantName name;
    final Instant createdAt;
    final LastLoginInfo lastLoginInfo;
    final Instant tenantRolesLastMaintained;

    private LockedTenant(TenantName name, Instant createdAt, LastLoginInfo lastLoginInfo, Instant tenantRolesLastMaintained) {
        this.name = requireNonNull(name);
        this.createdAt = requireNonNull(createdAt);
        this.lastLoginInfo = requireNonNull(lastLoginInfo);
        this.tenantRolesLastMaintained = requireNonNull(tenantRolesLastMaintained);
    }

    static LockedTenant of(Tenant tenant, Mutex lock) {
        return switch (tenant.type()) {
            case athenz -> new Athenz((AthenzTenant) tenant);
            case cloud -> new Cloud((CloudTenant) tenant);
            case deleted -> new Deleted((DeletedTenant) tenant);
        };
    }

    /** Returns a read-only copy of this */
    public abstract Tenant get();

    public abstract LockedTenant with(LastLoginInfo lastLoginInfo);

    public abstract LockedTenant with(Instant tenantRolesLastMaintained);

    public Deleted deleted(Instant deletedAt) {
        return new Deleted(new DeletedTenant(name, createdAt, deletedAt));
    }

    @Override
    public String toString() {
        return "tenant '" + name + "'";
    }


    /** A locked AthenzTenant. */
    public static class Athenz extends LockedTenant {

        private final AthenzDomain domain;
        private final Property property;
        private final Optional<PropertyId> propertyId;
        private final Optional<Contact> contact;

        private Athenz(TenantName name, AthenzDomain domain, Property property, Optional<PropertyId> propertyId,
                       Optional<Contact> contact, Instant createdAt, LastLoginInfo lastLoginInfo, Instant tenantRolesLastMaintained) {
            super(name, createdAt, lastLoginInfo, tenantRolesLastMaintained);
            this.domain = domain;
            this.property = property;
            this.propertyId = propertyId;
            this.contact = contact;
        }

        private Athenz(AthenzTenant tenant) {
            this(tenant.name(), tenant.domain(), tenant.property(), tenant.propertyId(), tenant.contact(), tenant.createdAt(), tenant.lastLoginInfo(), tenant.tenantRolesLastMaintained());
        }

        @Override
        public AthenzTenant get() {
            return new AthenzTenant(name, domain, property, propertyId, contact, createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

        public Athenz with(AthenzDomain domain) {
            return new Athenz(name, domain, property, propertyId, contact, createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

        public Athenz with(Property property) {
            return new Athenz(name, domain, property, propertyId, contact, createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

        public Athenz with(PropertyId propertyId) {
            return new Athenz(name, domain, property, Optional.of(propertyId), contact, createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

        public Athenz with(Contact contact) {
            return new Athenz(name, domain, property, propertyId, Optional.of(contact), createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

        @Override
        public LockedTenant with(LastLoginInfo lastLoginInfo) {
            return new Athenz(name, domain, property, propertyId, contact, createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

        @Override
        public LockedTenant with(Instant tenantRolesLastMaintained) {
            return new Athenz(name, domain, property, propertyId, contact, createdAt, lastLoginInfo, tenantRolesLastMaintained);
        }

    }


    /** A locked CloudTenant. */
    public static class Cloud extends LockedTenant {

        private final Optional<SimplePrincipal> creator;
        private final BiMap<PublicKey, SimplePrincipal> developerKeys;
        private final TenantInfo info;
        private final List<TenantSecretStore> tenantSecretStores;
        private final ArchiveAccess archiveAccess;
        private final Optional<Instant> invalidateUserSessionsBefore;

        private Cloud(TenantName name, Instant createdAt, LastLoginInfo lastLoginInfo, Optional<SimplePrincipal> creator,
                      BiMap<PublicKey, SimplePrincipal> developerKeys, TenantInfo info,
                      List<TenantSecretStore> tenantSecretStores, ArchiveAccess archiveAccess, Optional<Instant> invalidateUserSessionsBefore, Instant tenantRolesLastMaintained) {
            super(name, createdAt, lastLoginInfo, tenantRolesLastMaintained);
            this.developerKeys = ImmutableBiMap.copyOf(developerKeys);
            this.creator = creator;
            this.info = info;
            this.tenantSecretStores = tenantSecretStores;
            this.archiveAccess = archiveAccess;
            this.invalidateUserSessionsBefore = invalidateUserSessionsBefore;
        }

        private Cloud(CloudTenant tenant) {
            this(tenant.name(), tenant.createdAt(), tenant.lastLoginInfo(), tenant.creator(), tenant.developerKeys(), tenant.info(), tenant.tenantSecretStores(), tenant.archiveAccess(), tenant.invalidateUserSessionsBefore(), tenant.tenantRolesLastMaintained());
        }

        @Override
        public CloudTenant get() {
            return new CloudTenant(name, createdAt, lastLoginInfo, creator, developerKeys, info, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        public Cloud withDeveloperKey(PublicKey key, Principal principal) {
            BiMap<PublicKey, SimplePrincipal> keys = HashBiMap.create(developerKeys);
            SimplePrincipal simplePrincipal = new SimplePrincipal(principal.getName());
            if (keys.containsKey(key))
                throw new IllegalArgumentException("Key " + KeyUtils.toPem(key) + " is already owned by " + keys.get(key));
            if (keys.inverse().containsKey(simplePrincipal))
                throw new IllegalArgumentException(principal + " is already associated with key " + KeyUtils.toPem(keys.inverse().get(simplePrincipal)));
            keys.put(key, simplePrincipal);
            return new Cloud(name, createdAt, lastLoginInfo, creator, keys, info, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        public Cloud withoutDeveloperKey(PublicKey key) {
            BiMap<PublicKey, SimplePrincipal> keys = HashBiMap.create(developerKeys);
            keys.remove(key);
            return new Cloud(name, createdAt, lastLoginInfo, creator, keys, info, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        public Cloud withInfo(TenantInfo newInfo) {
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, newInfo, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        @Override
        public LockedTenant with(LastLoginInfo lastLoginInfo) {
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, info, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        public Cloud withSecretStore(TenantSecretStore tenantSecretStore) {
            ArrayList<TenantSecretStore> secretStores = new ArrayList<>(tenantSecretStores);
            secretStores.add(tenantSecretStore);
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, info, secretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        public Cloud withoutSecretStore(TenantSecretStore tenantSecretStore) {
            ArrayList<TenantSecretStore> secretStores = new ArrayList<>(tenantSecretStores);
            secretStores.remove(tenantSecretStore);
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, info, secretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }

        public Cloud withArchiveAccess(ArchiveAccess archiveAccess) {
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, info, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore,tenantRolesLastMaintained);
        }

        public Cloud withInvalidateUserSessionsBefore(Instant invalidateUserSessionsBefore) {
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, info, tenantSecretStores, archiveAccess, Optional.of(invalidateUserSessionsBefore), tenantRolesLastMaintained);
        }

        @Override
        public LockedTenant with(Instant tenantRolesLastMaintained) {
            return new Cloud(name, createdAt, lastLoginInfo, creator, developerKeys, info, tenantSecretStores, archiveAccess, invalidateUserSessionsBefore, tenantRolesLastMaintained);
        }
    }


    /** A locked DeletedTenant. */
    public static class Deleted extends LockedTenant {

        private final Instant deletedAt;

        private Deleted(DeletedTenant tenant) {
            super(tenant.name(), tenant.createdAt(), tenant.lastLoginInfo(), Instant.EPOCH);
            this.deletedAt = tenant.deletedAt();
        }

        @Override
        public DeletedTenant get() {
            return new DeletedTenant(name, createdAt, deletedAt);
        }

        @Override
        public LockedTenant with(LastLoginInfo lastLoginInfo) {
            return this;
        }

        @Override
        public LockedTenant with(Instant tenantRolesLastMaintained) {
            return this;
        }
    }

}
