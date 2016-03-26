package com.rtlabs.reqtool.model.requirements;

import com.rtlabs.reqtool.model.requirements.impl.SpecificationImpl;

public class SpecificationImplFinal extends SpecificationImpl {

	@Override
	public Requirement createNewRequirement() {
		Requirement requirement = RequirementsFactory.eINSTANCE.createRequirement();
		requirement.setIdentifier(nextIdentifier);
		nextIdentifier += 1;
		return requirement;		
	}	
	
}
