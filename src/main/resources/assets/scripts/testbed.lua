function ListViewTest_AdapterView(pGui, parent, position, data, oldView, luacontext)
	local viewToRet;
	if oldView == nil then
		local inflator = LuaViewInflator.Create(luacontext);
		viewToRet = inflator:ParseFile("testbedadapter.xml", listView);
		local tvTitle = viewToRet:GetViewById("testBedTitle");
		tvTitle:SetText(data);
		inflator:Free();
	else
		viewToRet = oldView;
		local tvTitle = viewToRet:GetViewById("testBedTitle");
		tvTitle:SetText(data);
	end
	return viewToRet;
end

function DatePicker_PositiveButton(pGui)
    
end

function DatePicker_NegativeButton(pGui)

end

function TimePicker_PositiveButton(pGui)

end

function TimePicker_NegativeButton(pGui)

end

function ListViewTest_AdapterView_ItemSelected(pGui, listView, detailView, position, data)
	local form = LuaForm.GetActiveForm();
	if position == 0 then
		LuaForm.CreateWithUI(form:GetContext(), "formTest", "form.xml");
	elseif position == 1 then
		LuaForm.CreateWithUI(form:GetContext(), "hsvTest", "hsv.xml");
	elseif position == 2 then
		LuaForm.CreateWithUI(form:GetContext(), "svTest", "sv.xml");
	elseif position == 3 then
		LuaForm.CreateWithUI(form:GetContext(), "mapTest", "map.xml");
	elseif position == 4 then
		LuaDialog.MessageBox(form:GetContext(), "Title", "Message");
	elseif position == 5 then
		local datePicker = LuaDialog.Create(form:GetContext(), LuaDialog_DIALOG_TYPE_DATEPICKER);
		datePicker:SetPositiveButton("Ok", LuaTranslator.Register(datePicker, "DatePicker_PositiveButton"));
		datePicker:SetNegativeButton("Cancel", LuaTranslator.Register(datePicker, "DatePicker_NegativeButton"));
		datePicker:SetTitle("Title");
		datePicker:SetMessage("Message");
		datePicker:SetDateManual(17, 7, 1985);
		datePicker:Show();
	elseif position == 6 then
		local timePicker = LuaDialog.Create(form:GetContext(), LuaDialog_DIALOG_TYPE_TIMEPICKER);
		timePicker:SetPositiveButton("Ok", LuaTranslator.Register(timePicker, "TimePicker_PositiveButton"));
		timePicker:SetNegativeButton("Cancel", LuaTranslator.Register(timePicker, "TimePicker_NegativeButton"));
		timePicker:SetTitle("Title");
		timePicker:SetMessage("Message");
		timePicker:SetTimeManual(17, 7);
		timePicker:Show();
	elseif position == 7 then
		LuaToast.Show(form:GetContext(), "Toast test", 2000);
	elseif position == 8 then
		LuaForm.CreateWithUI(form:GetContext(), "mapTest", "map.xml");
	else
		LuaForm.CreateWithUI(form:GetContext(), "backendTest", "backend.xml");
	end
end

function ListViewTest_Constructor(pGUI, luacontext)
	local pAdapter = LGAdapterView.Create(luacontext, "ListAdapterTest");
	pAdapter:RegisterEventFunction("ItemSelected", LuaTranslator.Register(pAdapter, "ListViewTest_AdapterView_ItemSelected"));
	pAdapter:AddValue(0, "Form Ui");
	pAdapter:AddValue(1, "Horizontal Scroll View");
	pAdapter:AddValue(2, "Vertical Scroll View");
	pAdapter:AddValue(3, "Map");
	pAdapter:AddValue(4, "Message Box");
	pAdapter:AddValue(5, "Date Picker Dialog");
	pAdapter:AddValue(6, "Time Picker Dialog");
	pAdapter:AddValue(7, "Toast");
	RegisterGuiEvent("ListAdapterTest", GUIEVENT_ADAPTERVIEW, "ListViewTest_AdapterView");
	pGUI:SetAdapter(pAdapter);
end

RegisterGuiEvent("ListViewTest", GUIEVENT_CREATE, "ListViewTest_Constructor");