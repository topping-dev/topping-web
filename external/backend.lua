function ListViewTest_AdapterView(pGui, parent, position, data, oldView, luacontext)
	local viewToRet;
	if oldView == nil then
		local inflator = LuaViewInflator.Create(luacontext);
		viewToRet = inflator:ParseFile("testbedAdapter.xml", listView);
		local tvTitle = viewToRet:GetViewById("testBedTitle");
		tvTitle:SetText(data);
		inflator:Free();
	else
		viewToRet = oldView;
	end
	return viewToRet;
end

function HttpClient_Finished(pGui, data)
end

function ListViewTest_AdapterView_ItemSelected(pGui, listView, detailView, position, data)
	local form = LuaForm.GetActiveForm();
	if position == 0 then
		local buffer = LuaBuffer.Create(5);
		buffer:SetByte(0, 1);
		print(buffer:GetByte(0));
	elseif position == 1 then
		local color = LuaColor.FromString("0xff000000");
		print(color.GetColorValue());
	elseif position == 2 then
		local date = LuaDate.Now();
		print(date:GetDay());
	elseif position == 3 then
		--Defines
	elseif position == 4 then
		local httpClient = LuaHttpClient.Create("asd");
		httpClient:RegisterEventFunction("OnFinish", LuaTranslator.Register(httpClient, "HttpClient_Finished"));
		httpClient:StartAsyncLoad("http://www.google.com", "", "asd");
	elseif position == 5 then

	elseif position == 6 then

	elseif position == 7 then

	end
end

function ListViewTest_Constructor(pGUI, luacontext)
	local pAdapter = LGAdapterView.Create(luacontext, "ListAdapterTest");
	pAdapter:RegisterEventFunction("ItemSelected", LuaTranslator.Register(pAdapter, "ListViewTest_AdapterView_ItemSelected"));
	pAdapter:AddValue(0, "Buffer Test");
	pAdapter:AddValue(1, "Color Test");
	pAdapter:AddValue(2, "Date Test");
	pAdapter:AddValue(3, "Defines Test");
	pAdapter:AddValue(4, "HttpClient Test");
	pAdapter:AddValue(5, "JSON Test");
	pAdapter:AddValue(6, "Resource Test");
	pAdapter:AddValue(7, "Store Test");
	RegisterGuiEvent("ListAdapterTest", GUIEVENT_ADAPTERVIEW, "ListViewTest_AdapterView");
	pGUI:SetAdapter(pAdapter);
end

RegisterGuiEvent("ListViewTest", GUIEVENT_CREATE, "ListViewTest_Constructor");