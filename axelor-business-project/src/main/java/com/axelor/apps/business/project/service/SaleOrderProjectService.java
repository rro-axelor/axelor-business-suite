package com.axelor.apps.business.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskManagementRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderProjectService extends SaleOrderRepository{

	@Inject
	protected GeneralService generalService;

	@Transactional
	public ProjectTask generateProject(SaleOrder saleOrder){
		ProjectTask project = new ProjectTask();
		project.setTypeSelect(ProjectTaskRepository.TYPE_PROJECT);
		project.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
		project.setName(saleOrder.getFullName());
		project.setCompany(saleOrder.getCompany());
		project.setClientPartner(saleOrder.getClientPartner());
		project.setAssignedTo(saleOrder.getSalemanUser());
		project.setSaleOrder(saleOrder);
		project.setProgress(0);
		project.setImputable(true);
		project.setInvoicingTypeSelect(ProjectTaskRepository.INVOICING_TYPE_NONE);
		project.addMembersUserSetItem(saleOrder.getSalemanUser());
		Product product = generalService.getGeneral().getProductInvoicingProjectTask();
		project.setProduct(product);
		project.setQty(BigDecimal.ONE);
		project.setPrice(saleOrder.getCompanyExTaxTotal());
		project.setUnit(product.getUnit());
		project.setExTaxTotal(saleOrder.getCompanyExTaxTotal());
		saleOrder.setProject(project);
		Beans.get(ProjectTaskManagementRepository.class).save(project);
		save(saleOrder);
		return project;
	}

	@Transactional
	public List<Long> generateTasks(SaleOrder saleOrder){
		List<Long> listId = new ArrayList<Long>();
		List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
		for (SaleOrderLine saleOrderLine : saleOrderLineList) {
			Product product = saleOrderLine.getProduct();
			if(ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect()) && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE){
				ProjectTask task = new ProjectTask();
				task.setTypeSelect(ProjectTaskRepository.TYPE_TASK);
				task.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
				task.setProject(saleOrder.getProject());
				task.setName(saleOrderLine.getFullName());
				task.setAssignedTo(saleOrder.getSalemanUser());
				task.setProgress(0);
				task.setImputable(true);
				task.setProduct(product);
				task.setQty(saleOrderLine.getQty());
				task.setPrice(saleOrderLine.getPrice());
				task.setUnit(saleOrderLine.getUnit());
				task.setExTaxTotal(saleOrderLine.getCompanyExTaxTotal());
				saleOrderLine.setProject(task);
				Beans.get(ProjectTaskManagementRepository.class).save(task);
				JPA.save(saleOrderLine);
				listId.add(task.getId());
			}
		}
		return listId;
	}

}
