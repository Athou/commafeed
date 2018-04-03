package com.commafeed.backend.dao;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.IStorageModelDAO;
import com.commafeed.backend.model.AbstractModel;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public abstract class GenericDAO<Model extends AbstractModel> extends AbstractDAO<Model> {

	private HibernateQueryFactory factory;
	protected IStorageModelDAO<Model> storage;

	protected GenericDAO(SessionFactory sessionFactory, IStorageModelDAO
			storage) {
		this(sessionFactory);
		this.storage = storage;
	}

	protected GenericDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
		this.factory = new HibernateQueryFactory(() -> currentSession());
		this.storage = null;
	}

	protected HibernateQueryFactory query() {
		return factory;
	}

	public void saveOrUpdate(Model model) {
		// Use the old persist(model) only if the new database isn't in
		// production
		if (!MigrationToggles.isReadAndWriteOn()) {
			persist(model);
		}
		// Use the new database to shadowwrites only if we're at or past this
		// step
		if (MigrationToggles.isShadowWritesOn()) {
			saveOrUpdateToStorage(model);
		}
	}

	public void saveOrUpdate(Collection<Model> models) {
		// Use the old persist(model) only if the new database isn't in
		// production
		if (!MigrationToggles.isReadAndWriteOn()) {
			models.forEach(m -> persist(m));
		}
		// Use the new database to shadowwrites only if we're at or past this
		// step
		if (MigrationToggles.isShadowWritesOn()) {
			models.forEach(model -> saveOrUpdateToStorage(model));
		}
	}

	public void update(Model model) {
		// Use the old persist(model) only if the new database isn't in
		// production
		if (!MigrationToggles.isReadAndWriteOn()) {
			currentSession().merge(model);
		}
		// Use the new database to shadowwrites only if we're at or past this
		// step
		if (MigrationToggles.isShadowWritesOn()) {
			saveOrUpdateToStorage(model);
		}
	}

	public Model findById(Long id) {
		Model returnValue = null;
		// Use the old db value only if the new database isn't in
		// production
		if (!MigrationToggles.isReadAndWriteOn()) {
			returnValue = get(id);
		}
		// Check if we're specifically at the shadow read step.
		// Run asynchronous code to check if the shadow read is consistent
		// with the regular read
		if (!MigrationToggles.isReadAndWriteOn() && MigrationToggles
				.isShadowReadsOn()) {
			final Model valueFoundInDb = returnValue;
			// Asynchronous code
			CompletableFuture.runAsync(() -> {
				this.storage.isModelConsistent(valueFoundInDb);
			});
		}
		if (MigrationToggles.isReadAndWriteOn()) {
			returnValue = this.storage.read(id);
		}
		return returnValue;
	}

	public void delete(Model model) {
		if (!MigrationToggles.isReadAndWriteOn()) {
			if (model != null) {
				currentSession().delete(model);
			}
		}
		if (MigrationToggles.isShadowWritesOn()) {
			deleteModelFromStorage(model);
		}
	}

	public int delete(Collection<Model> models) {
		if (!MigrationToggles.isReadAndWriteOn()) {
			models.forEach(m -> delete(m));
		}
		if (MigrationToggles.isShadowWritesOn()) {
			models.forEach(m -> deleteModelFromStorage(m));
		}
		return models.size();
	}

	protected void saveOrUpdateToStorage(Model model) {
		boolean isModelAlreadyInStorage = this.storage.exists(model);
		if (isModelAlreadyInStorage) {
			this.storage.update(model);
		} else {
			this.storage.create(model);
		}
	}

	private void deleteModelFromStorage(Model model) {
		boolean isModelInStorage = this.storage.exists(model);
		if (isModelInStorage) {
			this.storage.delete(model);
		}
	}

	public void supercedeIStorageModelDAOForTests(IStorageModelDAO storage) {
		this.storage = storage;
	}
}
