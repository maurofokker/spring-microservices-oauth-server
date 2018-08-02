insert into oauth_client_details(client_id, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove)
values ('webapp', '{noop}secret', 'trust,read,write', 'password,authorization_code, refresh_token', null, null, 36000, 36000, null, true);

insert into oauth_client_details(client_id, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove)
values ('resource1', '{noop}secret', 'trust,read,write', 'password,authorization_code, refresh_token', null, null, 36000, 36000, null, true);