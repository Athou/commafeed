package com.commafeed.backend.dao.newstorage;

import com.commafeed.backend.model.AbstractModel;

public interface IStorageModelDAO<Model extends AbstractModel> {

    public boolean exists(Model model);
    public void create(Model model);
    public Model read(Model model);
    public Model read(Long id);
    public Model update(Model model);
    public Model delete(Model model);
    public void serialize();
    public void deserialize();
    public boolean isModelConsistent(Model model);
}
