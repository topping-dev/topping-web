function GetJayrockRPCJson(funcName, ...)
	local str = "{ method: " .. funcName .. ", id:1, params:[";
	local count = 0;
	for i,v in ipairs(arg) do
        str = str .. "\"" .. tostring(v) .. "\"";
        count = count + 1;
        if table.getn(arg) ~= count then
        	str = str .. ",";
        end 
    end
    str = str .. "]}";
    print(str);
    return str;
end