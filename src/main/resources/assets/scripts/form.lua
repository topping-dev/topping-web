function FormTestLL_Constructor(pGUI, luacontext)
	local button = pGUI:GetViewById("formTestButton");
	button:RegisterEventFunction("Click", LuaTranslator.Register(button, "TestButton_Click"));
	local checkbox = pGUI:GetViewById("formTestCheckBox");
	checkbox:RegisterEventFunction("CheckedChanged", LuaTranslator.Register(checkbox, "TestCheckBox_CheckedChanged"));
	local combobox = pGUI:GetViewById("formTestComboBox");
	combobox:AddComboItem("Item 1", 1);
	combobox:AddComboItem("Item 2", 2);
	combobox:AddComboItem("Item 3", 3);
	combobox:AddComboItem("Item 4", 4);
	combobox:RegisterEventFunction("Changed", LuaTranslator.Register(combobox, "TestComboBox_Changed"));
	local edittext = pGUI:GetViewById("formTestEt");
end

function TestCheckBox_CheckedChanged(pGUI, context, isChecked)
	LuaToast.Show(context, "CheckBox value is " .. tostring(isChecked), 1000);
end

function TestButton_Click(pGUI, context)
	LuaToast.Show(context, "Test button clicked", 1000);
end

function TestComboBox_Changed(pGUI, context, name, value)
	LuaToast.Show(context, "Combobox id " .. name, 1000);
end

RegisterGuiEvent("formTestLL", GUIEVENT_CREATE, "FormTestLL_Constructor");