package me.kelei.wa.services;

import me.kelei.wa.entities.WaUser;
import me.kelei.wa.redis.dao.IWaDao;
import me.kelei.wa.utils.JYWaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Created by kelei on 2016/9/20.
 */
@Service
public class WaServiceImpl implements IWaService {

    @Autowired
    private IWaDao waDao;

    public WaUser login(String pid, String password) {
        WaUser waUser = waDao.getWaUser(pid);
        if(waUser == null){
            waUser = JYWaUtil.getJYWaUser(pid, password);
            if(waUser != null) waDao.saveWaUser(waUser);
        }
        return waUser;
    }
}
