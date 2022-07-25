package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.CommunityPharmacy;
import org.fhi360.ddd.domain.Inventory;

import org.fhi360.ddd.domain.Regimen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.rmi.activation.ActivationGroupDesc;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByCommunityPharmacy(CommunityPharmacy communityPharmacy);

    Inventory findByRegimen(Regimen regimen);
}
