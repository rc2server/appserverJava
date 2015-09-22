--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Name: rcfile_notifyd(); Type: FUNCTION; Schema: public; Owner: rc2
--

CREATE FUNCTION rcfile_notifyd() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
perform pg_notify(cast('rcfile' as text), 'd' || cast(old.id as text));
return new;
END;
$$;


ALTER FUNCTION public.rcfile_notifyd() OWNER TO rc2;

--
-- Name: rcfile_notifyi(); Type: FUNCTION; Schema: public; Owner: rc2
--

CREATE FUNCTION rcfile_notifyi() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
perform pg_notify(cast('rcfile' as text), 'i' || cast(new.id as text) || '/0/0');
return new;
END;
$$;


ALTER FUNCTION public.rcfile_notifyi() OWNER TO rc2;

--
-- Name: rcfile_notifyu(); Type: FUNCTION; Schema: public; Owner: rc2
--

CREATE FUNCTION rcfile_notifyu() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
perform pg_notify(cast('rcfile' as text), 'u' || cast(old.id as text) || '/0/0');
return new;
END;
$$;


ALTER FUNCTION public.rcfile_notifyu() OWNER TO rc2;

--
-- Name: update_lastmodified_column(); Type: FUNCTION; Schema: public; Owner: rc2
--

CREATE FUNCTION update_lastmodified_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   NEW.lastmodified = now(); 
   RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_lastmodified_column() OWNER TO rc2;

--
-- Name: update_version_column(); Type: FUNCTION; Schema: public; Owner: rc2
--

CREATE FUNCTION update_version_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   NEW.version = NEW.version + 1; 
   RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_version_column() OWNER TO rc2;

--
-- Name: crashdata_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE crashdata_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE crashdata_seq OWNER TO rc2;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: crashdata; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE crashdata (
    id integer DEFAULT nextval('crashdata_seq'::regclass) NOT NULL,
    userid integer,
    reportdate timestamp without time zone DEFAULT now() NOT NULL,
    crashdata bytea
);


ALTER TABLE crashdata OWNER TO rc2;

--
-- Name: ldapserver; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE ldapserver (
    id integer NOT NULL,
    name character varying(40) NOT NULL,
    hostname character varying(80) NOT NULL,
    port integer DEFAULT 389,
    basedn character varying(100) NOT NULL,
    hosturi character varying(80),
    loginkey character varying(10) NOT NULL
);


ALTER TABLE ldapserver OWNER TO rc2;

--
-- Name: logapp_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE logapp_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE logapp_seq OWNER TO rc2;

--
-- Name: logapp; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE logapp (
    id integer DEFAULT nextval('logapp_seq'::regclass) NOT NULL,
    name character varying(40) NOT NULL,
    apikey character varying(40) NOT NULL
);


ALTER TABLE logapp OWNER TO rc2;

--
-- Name: logentry_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE logentry_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE logentry_seq OWNER TO rc2;

--
-- Name: logentry; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE logentry (
    id integer DEFAULT nextval('logentry_seq'::regclass) NOT NULL,
    appid integer NOT NULL,
    uaid integer,
    clientidentifier character varying(80),
    datereceived timestamp without time zone DEFAULT now() NOT NULL,
    level integer,
    context integer,
    versionstr character varying(60),
    message character varying(1024)
);


ALTER TABLE logentry OWNER TO rc2;

--
-- Name: logintokens_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE logintokens_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE logintokens_seq OWNER TO rc2;

--
-- Name: logintokens; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE logintokens (
    id integer DEFAULT nextval('logintokens_seq'::regclass) NOT NULL,
    datecreated timestamp without time zone DEFAULT now() NOT NULL,
    userid integer NOT NULL,
    series_ident bigint NOT NULL,
    token_ident bigint NOT NULL
);


ALTER TABLE logintokens OWNER TO rc2;

--
-- Name: logua_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE logua_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE logua_seq OWNER TO rc2;

--
-- Name: logua; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE logua (
    id integer DEFAULT nextval('logua_seq'::regclass) NOT NULL,
    uastring character varying(200) NOT NULL
);


ALTER TABLE logua OWNER TO rc2;

--
-- Name: rcfile_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE rcfile_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE rcfile_seq OWNER TO rc2;

--
-- Name: rcfile; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rcfile (
    id integer DEFAULT nextval('rcfile_seq'::regclass) NOT NULL,
    wspaceid integer NOT NULL,
    name character varying(200) NOT NULL,
    datecreated timestamp without time zone DEFAULT now() NOT NULL,
    lastmodified timestamp without time zone DEFAULT now() NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    filesize integer,
    objtype character varying(10) DEFAULT 'file'::character varying
);


ALTER TABLE rcfile OWNER TO rc2;

--
-- Name: rcfiledata; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rcfiledata (
    id integer NOT NULL,
    bindata bytea
);


ALTER TABLE rcfiledata OWNER TO rc2;

--
-- Name: rcnoance_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE rcnoance_seq
    START WITH 10
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE rcnoance_seq OWNER TO rc2;

--
-- Name: rcnoance; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rcnoance (
    id integer DEFAULT nextval('rcnoance_seq'::regclass) NOT NULL,
    userid integer NOT NULL,
    requesttime timestamp without time zone NOT NULL,
    action character varying(30) NOT NULL,
    noance character varying(100) NOT NULL
);


ALTER TABLE rcnoance OWNER TO rc2;

--
-- Name: rctemplate_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE rctemplate_seq
    START WITH 10
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE rctemplate_seq OWNER TO rc2;

--
-- Name: rctemplate; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rctemplate (
    id integer DEFAULT nextval('rctemplate_seq'::regclass) NOT NULL,
    name character varying(20) NOT NULL,
    content text
);


ALTER TABLE rctemplate OWNER TO rc2;

--
-- Name: rcuser_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE rcuser_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE rcuser_seq OWNER TO rc2;

--
-- Name: rcuser; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rcuser (
    id integer DEFAULT nextval('rcuser_seq'::regclass) NOT NULL,
    login character varying(40) NOT NULL,
    firstname character varying(20),
    lastname character varying(20),
    email character varying(80) NOT NULL,
    passworddata character varying(200),
    admin boolean DEFAULT false NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    lastmodified timestamp without time zone DEFAULT now() NOT NULL,
    force_pass_reset boolean DEFAULT false NOT NULL,
    ldapid integer,
    ldaplogin character varying(80)
);


ALTER TABLE rcuser OWNER TO rc2;

--
-- Name: rcworkspace_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE rcworkspace_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE rcworkspace_seq OWNER TO rc2;

--
-- Name: rcworkspace; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rcworkspace (
    id integer DEFAULT nextval('rcworkspace_seq'::regclass) NOT NULL,
    name character varying(60) NOT NULL,
    userid integer NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    lastaccess timestamp without time zone DEFAULT now() NOT NULL,
    datecreated timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE rcworkspace OWNER TO rc2;

--
-- Name: rcworkspacedata; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE rcworkspacedata (
    id integer NOT NULL,
    bindata bytea
);


ALTER TABLE rcworkspacedata OWNER TO rc2;

--
-- Name: sessionimage_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE sessionimage_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sessionimage_seq OWNER TO rc2;

--
-- Name: sessionimage; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE sessionimage (
    id integer DEFAULT nextval('sessionimage_seq'::regclass) NOT NULL,
    sessionid integer NOT NULL,
    batchid integer DEFAULT 0 NOT NULL,
    name character varying(80) NOT NULL,
    datecreated timestamp without time zone DEFAULT now() NOT NULL,
    imgdata bytea
);


ALTER TABLE sessionimage OWNER TO rc2;

--
-- Name: sessionrecord_seq; Type: SEQUENCE; Schema: public; Owner: rc2
--

CREATE SEQUENCE sessionrecord_seq
    START WITH 10
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sessionrecord_seq OWNER TO rc2;

--
-- Name: sessionrecord; Type: TABLE; Schema: public; Owner: rc2; Tablespace: 
--

CREATE TABLE sessionrecord (
    id integer DEFAULT nextval('sessionrecord_seq'::regclass) NOT NULL,
    wspaceid integer NOT NULL,
    startdate timestamp without time zone DEFAULT now() NOT NULL,
    enddate timestamp without time zone
);


ALTER TABLE sessionrecord OWNER TO rc2;

--
-- Name: crashdata_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY crashdata
    ADD CONSTRAINT crashdata_pkey PRIMARY KEY (id);


--
-- Name: ldapserver_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY ldapserver
    ADD CONSTRAINT ldapserver_pkey PRIMARY KEY (id);


--
-- Name: logapp_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY logapp
    ADD CONSTRAINT logapp_pkey PRIMARY KEY (id);


--
-- Name: logentry_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY logentry
    ADD CONSTRAINT logentry_pkey PRIMARY KEY (id);


--
-- Name: logintokens_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY logintokens
    ADD CONSTRAINT logintokens_pkey PRIMARY KEY (id);


--
-- Name: logua_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY logua
    ADD CONSTRAINT logua_pkey PRIMARY KEY (id);


--
-- Name: rcfile_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcfile
    ADD CONSTRAINT rcfile_pkey PRIMARY KEY (id);


--
-- Name: rcfiledata_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcfiledata
    ADD CONSTRAINT rcfiledata_pkey PRIMARY KEY (id);


--
-- Name: rcnoance_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcnoance
    ADD CONSTRAINT rcnoance_pkey PRIMARY KEY (id);


--
-- Name: rctemplate_name_key; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rctemplate
    ADD CONSTRAINT rctemplate_name_key UNIQUE (name);


--
-- Name: rctemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rctemplate
    ADD CONSTRAINT rctemplate_pkey PRIMARY KEY (id);


--
-- Name: rcuser_login_unique; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcuser
    ADD CONSTRAINT rcuser_login_unique UNIQUE (login);


--
-- Name: rcuser_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcuser
    ADD CONSTRAINT rcuser_pkey PRIMARY KEY (id);


--
-- Name: rcworkspace_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcworkspace
    ADD CONSTRAINT rcworkspace_pkey PRIMARY KEY (id);


--
-- Name: rcworkspacedata_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY rcworkspacedata
    ADD CONSTRAINT rcworkspacedata_pkey PRIMARY KEY (id);


--
-- Name: sessionimage_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY sessionimage
    ADD CONSTRAINT sessionimage_pkey PRIMARY KEY (id);


--
-- Name: sessionrecord_pkey; Type: CONSTRAINT; Schema: public; Owner: rc2; Tablespace: 
--

ALTER TABLE ONLY sessionrecord
    ADD CONSTRAINT sessionrecord_pkey PRIMARY KEY (id);


--
-- Name: rcuser_email_idx; Type: INDEX; Schema: public; Owner: rc2; Tablespace: 
--

CREATE UNIQUE INDEX rcuser_email_idx ON rcuser USING btree (email);


--
-- Name: rcuser_login_idx; Type: INDEX; Schema: public; Owner: rc2; Tablespace: 
--

CREATE UNIQUE INDEX rcuser_login_idx ON rcuser USING btree (login);


--
-- Name: sessionimage_sessionrec_idx; Type: INDEX; Schema: public; Owner: rc2; Tablespace: 
--

CREATE INDEX sessionimage_sessionrec_idx ON sessionimage USING btree (sessionid);


--
-- Name: tokens_idx; Type: INDEX; Schema: public; Owner: rc2; Tablespace: 
--

CREATE UNIQUE INDEX tokens_idx ON logintokens USING btree (userid, series_ident);


--
-- Name: rcfile_trigger_notifyd; Type: TRIGGER; Schema: public; Owner: rc2
--

CREATE TRIGGER rcfile_trigger_notifyd AFTER DELETE ON rcfile FOR EACH ROW EXECUTE PROCEDURE rcfile_notifyd();


--
-- Name: rcfile_trigger_notifyi; Type: TRIGGER; Schema: public; Owner: rc2
--

CREATE TRIGGER rcfile_trigger_notifyi AFTER INSERT ON rcfile FOR EACH ROW EXECUTE PROCEDURE rcfile_notifyi();


--
-- Name: rcfile_trigger_notifyu; Type: TRIGGER; Schema: public; Owner: rc2
--

CREATE TRIGGER rcfile_trigger_notifyu AFTER UPDATE ON rcfile FOR EACH ROW EXECUTE PROCEDURE rcfile_notifyu();


--
-- Name: rcuser_update_lastmod; Type: TRIGGER; Schema: public; Owner: rc2
--

CREATE TRIGGER rcuser_update_lastmod BEFORE UPDATE ON rcuser FOR EACH ROW EXECUTE PROCEDURE update_lastmodified_column();


--
-- Name: crashdata_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY crashdata
    ADD CONSTRAINT crashdata_userid_fkey FOREIGN KEY (userid) REFERENCES rcuser(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: logentry_appid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY logentry
    ADD CONSTRAINT logentry_appid_fkey FOREIGN KEY (appid) REFERENCES logapp(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: logentry_uaid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY logentry
    ADD CONSTRAINT logentry_uaid_fkey FOREIGN KEY (uaid) REFERENCES logua(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: rcfile_wspaceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY rcfile
    ADD CONSTRAINT rcfile_wspaceid_fkey FOREIGN KEY (wspaceid) REFERENCES rcworkspace(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: rcfiledata_file_fk; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY rcfiledata
    ADD CONSTRAINT rcfiledata_file_fk FOREIGN KEY (id) REFERENCES rcfile(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: rcuser_ldapid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY rcuser
    ADD CONSTRAINT rcuser_ldapid_fkey FOREIGN KEY (ldapid) REFERENCES ldapserver(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: rcworkspace_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY rcworkspace
    ADD CONSTRAINT rcworkspace_userid_fkey FOREIGN KEY (userid) REFERENCES rcuser(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: rcworkspacedata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY rcworkspacedata
    ADD CONSTRAINT rcworkspacedata_id_fkey FOREIGN KEY (id) REFERENCES rcworkspace(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sessionimage_sessionrec_fk; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY sessionimage
    ADD CONSTRAINT sessionimage_sessionrec_fk FOREIGN KEY (sessionid) REFERENCES sessionrecord(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sessionrecord_wspace_fk; Type: FK CONSTRAINT; Schema: public; Owner: rc2
--

ALTER TABLE ONLY sessionrecord
    ADD CONSTRAINT sessionrecord_wspace_fk FOREIGN KEY (wspaceid) REFERENCES rcworkspace(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;



--
-- PostgreSQL database dump complete
--

